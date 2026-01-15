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

import java.awt.*;

public class GameScreen implements Screen {

    final MainGame game;

    //Cameras
    OrthographicCamera camera;
    OrthographicCamera uiCamera;

    //Logica do jogo
    DungeonPT2 dungeon;

    //Texturas
    Texture imgChao, imgParede, imgLuz, sheetAstronauta;
    Animation<TextureRegion> animBaixo, animCima, animEsq, animDir;
    TextureRegion frameAtual;

    // Variáveis
    int tamanhoTile = 32;
    int larguraMapa = 250;
    int alturaMapa = 250;
    float playerX, playerY;
    float velocidade = 150f;
    int direcaoAtual = 0;
    float tempoAnimacao = 0f;

    // HUD Variáveis
    int vida = 100;


    //Onde inicia tudo (antigo create)
    public GameScreen (final MainGame game){
        this.game=game;

        //Câmera do Mundo (Zoom perto)
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        camera.zoom = 0.5f;

        //Câmera do HUD (Sem Zoom, fixa)
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, 1280, 720);

        // Carregando Assets
        imgChao = new Texture("chao.png");
        imgParede = new Texture("parede.png");
        imgLuz = new Texture("luz.png");
        sheetAstronauta = new Texture("astronauta_anim.png");

        // Filtros Pixel Art
        sheetAstronauta.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        imgChao.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        imgParede.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);


        // Configura Animações
        TextureRegion[][] tmp = TextureRegion.split(sheetAstronauta, 32, 32);
        animBaixo = new Animation<>(0.15f, tmp[0]);
        animDir   = new Animation<>(0.15f, tmp[1]);
        animEsq   = new Animation<>(0.15f, tmp[2]);
        animCima  = new Animation<>(0.15f, tmp[3]);

        // Gera Dungeon
        dungeon = new DungeonPT2(larguraMapa, alturaMapa);
        dungeon.gerarDungeon(40, 1000);
        Rectangle primeiraSala = dungeon.getSalas().get(0);
        playerX = (primeiraSala.x * tamanhoTile) + (primeiraSala.width * tamanhoTile / 2f);
        playerY = (primeiraSala.y * tamanhoTile) + (primeiraSala.height * tamanhoTile / 2f);


    }

    @Override
    public void render(float delta){

        //logica de andar
        boolean andou = moverJogador(delta);

        if(andou){
            tempoAnimacao+=delta;
        }else{
            tempoAnimacao=0;
        }

        if (direcaoAtual == 0) frameAtual = animBaixo.getKeyFrame(tempoAnimacao, true);
        else if (direcaoAtual == 1) frameAtual = animCima.getKeyFrame(tempoAnimacao, true);
        else if (direcaoAtual == 2) frameAtual = animEsq.getKeyFrame(tempoAnimacao, true);
        else if (direcaoAtual == 3) frameAtual = animDir.getKeyFrame(tempoAnimacao, true);

        // Limpeza de Tela (Cor marrom escura para evitar texture bleeding)
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Começar a desenhar o mundo

        //------------------
        //Inicializando camera
        //------------

        camera.position.set((int) playerX, (int)playerY,0); // esse 0 evita tremedeira
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();


        //Desenha o mapa

        int visao = 20;
        int centrox = (int) (playerX / tamanhoTile);
        int centroy = (int) (playerY / tamanhoTile);

        for(int x = centrox - visao; x < centrox + visao; x++) {
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

        // Desenha Player e Luz
        game.batch.draw(frameAtual, playerX - 16, playerY - 16);
        float luzSize = 1000;
        game.batch.draw(imgLuz, playerX - (luzSize/2), playerY - (luzSize/2), luzSize, luzSize);

        game.batch.end();

        //Desenhar o HUD (que fica travado

        // Escreve na tela usando a fonte do MainGame
        // Coordenadas: 0,0 é o canto inferior esquerdo.
        // 20, 580 é o canto superior esquerdo (quase no topo)
        // ---------------------------------------------------------
        // Desenhar o HUD (Interface)
        // ---------------------------------------------------------

        // 1. Atualiza a câmera UI
        uiCamera.update(); // É bom garantir

        // 2. AVISA O BATCH PARA USAR A CÂMERA DE UI AGORA
        game.batch.setProjectionMatrix(uiCamera.combined);

        game.batch.begin();

        game.font.setColor(1, 1, 1, 1);

        // Pega a altura atual da câmera (para funcionar em qualquer resolução)
        float altura = uiCamera.viewportHeight;
        float largura = uiCamera.viewportWidth;


        // TEXTO NO TOPO: Use 'altura - valor' em vez de numero fixo (580)
        // Assim, se a tela for pequena, o texto desce junto.
        game.font.draw(game.batch, "VIDA: " + vida + "%", 20, altura - 20);
        game.font.draw(game.batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, altura - 40);

        game.batch.end();
    }

    public boolean moverJogador(float dt){
        float novaX = playerX;
        float novaY = playerY;
        boolean moveu = false;

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) { novaY += velocidade * dt; direcaoAtual = 1; moveu = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) { novaY -= velocidade * dt; direcaoAtual = 0; moveu = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) { novaX -= velocidade * dt; direcaoAtual = 2; moveu = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) { novaX += velocidade * dt; direcaoAtual = 3; moveu = true; }

        if (!colideComParede(novaX, playerY)) playerX = novaX;
        if (!colideComParede(playerX, novaY)) playerY = novaY;

        return moveu;
    }

    private boolean colideComParede(float x, float y) {
        float margem = 14f;
        if (eParede(x - margem, y - margem)) return true;
        if (eParede(x + margem, y - margem)) return true;
        if (eParede(x - margem, y + margem)) return true;
        if (eParede(x + margem, y + margem)) return true;
        return false;
    }

    private boolean eParede (float x, float y){
        int tileX = (int) (x / tamanhoTile);
        int tileY = (int) (y / tamanhoTile);
        if (tileX < 0 || tileX >= larguraMapa || tileY < 0 || tileY >= alturaMapa) return true;
        return dungeon.getMapa()[tileX][tileY] == 1;
    }

    @Override
    public void resize(int width, int height) {
        // Atualiza a câmera do jogo (mantém o zoom)
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();

        // --- O PULO DO GATO ---
        // Reinicia a câmera de UI para ter exatamente o tamanho da tela (pixel por pixel)
        // O 'true' ali centraliza a câmera (opcional), mas o 'setToOrtho(false...)' define a origem no canto inferior esquerdo.
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
        sheetAstronauta.dispose();
    }

}
