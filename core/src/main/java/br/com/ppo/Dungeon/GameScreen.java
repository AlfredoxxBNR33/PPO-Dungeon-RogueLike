package br.com.ppo.Dungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen implements Screen {

    final MainGame game;

    // Cameras
    OrthographicCamera camera;
    OrthographicCamera uiCamera;

    // Lógica do jogo
    DungeonPT2 dungeon;
    Jogador jogador; // Nosso jogador, agora em uma classe separada

    // Texturas Gerais (o astronauta foi pra classe Jogador)
    Texture imgChao, imgParede, imgLuz, sheetTiro;
    Animation<TextureRegion> animacaoTiro;

    // Variáveis do Mapa
    int tamanhoTile = 32;
    int larguraMapa = 50; //250
    int alturaMapa = 50;  //250

    // Variáveis do sistema de tiro
    ArrayList<Tiro> listaTiros;
    float tempoRecarga = 0;

    // Variaveis do spawn de inimigos
    ArrayList<Inimigo> listaInimigo;


    public GameScreen(final MainGame game) {
        this.game = game;

        // Câmera do Mundo
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        camera.zoom = 0.5f;

        // Câmera do HUD
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, 1280, 720);

        // Carregando Assets
        imgChao = new Texture("chao.png");
        imgParede = new Texture("parede.png");
        imgLuz = new Texture("luz.png");
        sheetTiro = new Texture("tiro.png");

        // Lista de tiros
        listaTiros = new ArrayList<>();

        // Filtros Pixel Art
        imgChao.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        imgParede.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        sheetTiro.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Preparando a Animação do Tiro
        TextureRegion[][] tmpTiro = TextureRegion.split(sheetTiro, 32, 32);
        TextureRegion[] framesTiro = new TextureRegion[2 * 3];
        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                framesTiro[index++] = tmpTiro[i][j];
            }
        }
        animacaoTiro = new Animation<>(0.3f, framesTiro);

        // Gera a Dungeon
        dungeon = new DungeonPT2(larguraMapa, alturaMapa);
        dungeon.gerarDungeon(2, 1000);
        Rectangle primeiraSala = dungeon.getSalas().get(0);

        // Posição inicial baseada na primeira sala
        float startX = (primeiraSala.x * tamanhoTile) + (primeiraSala.width * tamanhoTile / 2f);
        float startY = (primeiraSala.y * tamanhoTile) + (primeiraSala.height * tamanhoTile / 2f);

        // Cria o jogador bem no meio da primeira sala
        jogador = new Jogador(startX, startY);
        // Cria inimigo
        listaInimigo = new ArrayList<>();

        //Pega a segunda sala gerada
        Rectangle segundaSala = dungeon.getSalas().get(1);
        float inimigoX = (segundaSala.x * tamanhoTile) + (segundaSala.width * tamanhoTile /2f);
        float inimigoY = (segundaSala.y * tamanhoTile) + (segundaSala.height * tamanhoTile /2f);

        listaInimigo.add((new Inimigo(inimigoX,inimigoY)));
    }

    @Override
    public void render(float delta) {

        // Atualiza a física e movimentação do jogador
        jogador.update(delta, dungeon, tamanhoTile, larguraMapa, alturaMapa);

        // Lógica de atirar
        if (tempoRecarga > 0) {
            tempoRecarga -= delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && tempoRecarga <= 0) {
            // Cria o tiro saindo da posição atual do jogador
            listaTiros.add(new Tiro(jogador.x - 15, jogador.y - 18, jogador.direcaoAtual, animacaoTiro));
            tempoRecarga = 0.4f;
        }

        // Atualizar tiros
        Iterator<Tiro> iterTiro = listaTiros.iterator();
        while (iterTiro.hasNext()) {
            Tiro t = iterTiro.next();
            t.update(delta,dungeon,tamanhoTile,larguraMapa,alturaMapa);

            boolean acertouAlvo = false;

            for(Inimigo inimigo : listaInimigo){
                if(t.retanguloColisao.overlaps(inimigo.getHitbox())){
                    inimigo.darDano(1);
                    acertouAlvo=true;
                    break;
                }
            }

            if(acertouAlvo || t.deveRemover){
                iterTiro.remove();
            }
        }

        Iterator<Inimigo> iterInimigo = listaInimigo.iterator();
        while(iterInimigo.hasNext()){
            Inimigo inimigo = iterInimigo.next();

            inimigo.update(delta, jogador.x, jogador.y, dungeon, tamanhoTile, larguraMapa, alturaMapa);

            if(inimigo.deveRemover){
                inimigo.dispose();
                iterInimigo.remove();
            }
        }

        // --- INÍCIO DO DESENHO ---
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Atualiza Câmera seguindo o jogador
        camera.position.set((int) jogador.x, (int) jogador.y, 0);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        // Desenha o mapa
        int visao = 20;
        int centrox = (int) (jogador.x / tamanhoTile);
        int centroy = (int) (jogador.y / tamanhoTile);

        for (int x = centrox - visao; x < centrox + visao; x++) {
            for (int y = centroy - visao; y < centroy + visao; y++) {
                if (x >= 0 && x < larguraMapa && y >= 0 && y < alturaMapa) {
                    if (dungeon.getMapa()[x][y] == 1) {
                        game.batch.draw(imgParede, x * tamanhoTile, y * tamanhoTile);
                    } else if (dungeon.getMapa()[x][y] == 0) {
                        game.batch.draw(imgChao, x * tamanhoTile, y * tamanhoTile);
                    }
                }
            }
        }

        // Desenha os tiros
        for (Tiro t : listaTiros) {
            t.render(game.batch);
        }
        // Desenha os inimigos
        for(Inimigo i : listaInimigo){
            i.render(game.batch);
        }
        // Desenha o jogador
        jogador.render(game.batch);

        //Desenha

        // Desenha a Luz
        float luzSize = 1000;
        game.batch.draw(imgLuz, jogador.x - (luzSize / 2), jogador.y - (luzSize / 2), luzSize, luzSize);

        game.batch.end();

        // --- DESENHO DO HUD ---
        uiCamera.update();
        game.batch.setProjectionMatrix(uiCamera.combined);
        game.batch.begin();
        game.font.setColor(1, 1, 1, 1);
        float altura = uiCamera.viewportHeight;

        // Puxa o valor da vida direto do objeto jogador
        game.font.draw(game.batch, "VIDA: " + jogador.vida + "%", 20, altura - 20);
        game.font.draw(game.batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, altura - 40);
        game.batch.end();
    }

    // Função pra checar paredes (deixei aqui porque o tiro usa também)
    private boolean eParede(float x, float y) {
        int tileX = (int) (x / tamanhoTile);
        int tileY = (int) (y / tamanhoTile);
        if (tileX < 0 || tileX >= larguraMapa || tileY < 0 || tileY >= alturaMapa) return true;
        return dungeon.getMapa()[tileX][tileY] == 1;
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        imgChao.dispose();
        imgParede.dispose();
        imgLuz.dispose();
        sheetTiro.dispose();
        jogador.dispose();

        for(Inimigo i : listaInimigo){
            i.dispose();
        }
    }
}
