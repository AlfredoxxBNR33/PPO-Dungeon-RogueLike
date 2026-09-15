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

    // algumas variaveis de mundo
    int numKills = 0;
    static final int KILLS_NECESSARIOS = 10;
    boolean chaveLiberada = false;
    float chaveX;
    float chaveY;
    java.awt.Rectangle hitboxChave;
    // Cameras
    OrthographicCamera camera;
    OrthographicCamera uiCamera;

    // Lógica do jogo
    DungeonPT2 dungeon;
    Jogador jogador; // Nosso jogador, agora em uma classe separada

    // Texturas Gerais (o astronauta foi pra classe Jogador)
    Texture imgChao, imgParede, imgLuz, sheetTiro, imgChave;
    Animation<TextureRegion> animacaoTiro;

    // Variáveis do Mapa
    int tamanhoTile = 32;
    int larguraMapa = 50; // 250
    int alturaMapa = 50; // 250
    int salas = 20;
    int tentativas = 1000;

    // Variáveis do sistema de tiro
    ArrayList<Tiro> listaTiros;
    float tempoRecarga = 0;

    // Variaveis do spawn de inimigos
    ArrayList<Inimigo> listaInimigo;
    float tempoSpawn = 0;
    float intervaloSpawn = 5f;
    int maxInimigos = 30; // Limite de inimigos na tela para evitar sobrecarga

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
        imgChave = new Texture("chave.png");

        // Lista de tiros
        listaTiros = new ArrayList<>();

        // Filtros Pixel Art
        imgChao.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        imgParede.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        sheetTiro.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        imgChave.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

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
        dungeon.gerarDungeon(salas, tentativas);
        Rectangle primeiraSala = dungeon.getSalas().get(0);

        // Posição inicial baseada na primeira sala
        float startX = (primeiraSala.x * tamanhoTile) + (primeiraSala.width * tamanhoTile / 2f);
        float startY = (primeiraSala.y * tamanhoTile) + (primeiraSala.height * tamanhoTile / 2f);

        // Cria o jogador bem no meio da primeira sala
        jogador = new Jogador(startX, startY);
        // Cria inimigo
        listaInimigo = new ArrayList<>();

        // Pega a segunda sala gerada
        Rectangle segundaSala = dungeon.getSalas().get(1);
        float inimigoX = (segundaSala.x * tamanhoTile) + (segundaSala.width * tamanhoTile / 2f);
        float inimigoY = (segundaSala.y * tamanhoTile) + (segundaSala.height * tamanhoTile / 2f);

        listaInimigo.add((new Inimigo(inimigoX, inimigoY)));
    }

    @Override
    public void render(float delta) {

        // Atualiza a física e movimentação do jogador
        jogador.update(delta, dungeon, tamanhoTile, larguraMapa, alturaMapa);

        if (jogador.getVida() <= 0) {
            game.setScreen(new GameoverScreen(game));
            dispose();
            return;
        }

        // Lógica de atirar
        if (tempoRecarga > 0) {
            tempoRecarga -= delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && tempoRecarga <= 0) {
            // Cria o tiro saindo da posição atual do jogador
            listaTiros.add(new Tiro(jogador.x - 15, jogador.y - 18, jogador.direcaoAtual, animacaoTiro));
            tempoRecarga = 0.4f;
        }
        tempoSpawn += delta;
        if (tempoSpawn >= intervaloSpawn && listaInimigo.size() < maxInimigos) {
            Rectangle salaSelecionada = dungeon.getSalas().get((int) (Math.random() * dungeon.getSalas().size()));

            // Tenta até 10 vezes encontrar uma posição segura
            boolean spawnouComSucesso = false;
            for (int tentativa = 0; tentativa < 10; tentativa++) {
                // Gera posição mais pro interior da sala, evitando bordas
                float offset = 0.2f; // Mantém 20% de distância das bordas
                float xx = (float) ((salaSelecionada.x + offset + Math.random() * (salaSelecionada.width - offset * 2))
                        * tamanhoTile);
                float yy = (float) ((salaSelecionada.y + offset + Math.random() * (salaSelecionada.height - offset * 2))
                        * tamanhoTile);

                Inimigo novoInimigo = new Inimigo(xx, yy);

                // Verifica colisão com outros inimigos (com distância mínima)
                boolean podeSpawnar = true;
                float distanciaMinima = 80f; // Pelo menos 80 pixels de distância
                for (Inimigo inimigo : listaInimigo) {
                    float dx = novoInimigo.x - inimigo.x;
                    float dy = novoInimigo.y - inimigo.y;
                    float distancia = (float) Math.sqrt(dx * dx + dy * dy);
                    if (distancia < distanciaMinima) {
                        podeSpawnar = false;
                        novoInimigo.dispose();
                        break;
                    }
                }

                if (podeSpawnar) {
                    listaInimigo.add(novoInimigo);
                    tempoSpawn = 0;
                    spawnouComSucesso = true;
                    break;
                }
            }
        }
        // Atualizar tiros
        Iterator<Tiro> iterTiro = listaTiros.iterator();
        while (iterTiro.hasNext()) {
            Tiro t = iterTiro.next();
            t.update(delta, dungeon, tamanhoTile, larguraMapa, alturaMapa);

            boolean acertouAlvo = false;

            for (Inimigo inimigo : listaInimigo) {
                if (t.retanguloColisao.overlaps(inimigo.getHitbox())) {
                    inimigo.darDano(1);
                    if (inimigo.getVida() <= 0) {
                        numKills++;
                        liberarChaveSeNecessario();
                    }
                    acertouAlvo = true;
                    break;
                }
            }

            if (acertouAlvo || t.deveRemover) {
                iterTiro.remove();
            }
        }

        Iterator<Inimigo> iterInimigo = listaInimigo.iterator();
        while (iterInimigo.hasNext()) {
            Inimigo inimigo = iterInimigo.next();

            inimigo.update(delta, jogador.x, jogador.y, dungeon, tamanhoTile, larguraMapa, alturaMapa, listaInimigo);

            // regra para dar dano
            if (inimigo.getHitbox().overlaps(jogador.getHitbox())) {
                jogador.levarDano(5);
                inimigo.morrer();
            }

            if (inimigo.deveRemover) {
                inimigo.dispose();
                iterInimigo.remove();
            }
        }

        if (chaveLiberada && hitboxChave.contains(jogador.x, jogador.y)) {
            game.setScreen(new VictoryScreen(game));
            dispose();
            return;
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
        for (Inimigo i : listaInimigo) {
            i.render(game.batch);
        }
        // Desenha o jogador
        jogador.render(game.batch);

        if (chaveLiberada) {
            game.batch.draw(imgChave, chaveX - 16, chaveY - 16, 32, 32);
        }

        // Desenha

        // Desenha a Luz
        float luzSize = 1000;
        game.batch.draw(imgLuz, jogador.x - (luzSize / 2), jogador.y - (luzSize / 2), luzSize, luzSize);

        game.batch.end();

        // --- DESENHO DO HUD ---
        uiCamera.update();
        game.batch.setProjectionMatrix(uiCamera.combined);
        game.batch.begin();
        float altura = uiCamera.viewportHeight;

        float escala = 3f; // Você pode testar 2f, 3f ou 4f para ver qual tamanho encaixa melhor na tela
        float barraWidth = jogador.frameBarraVidaAtual.getRegionWidth() * escala;
        float barraHeight = jogador.frameBarraVidaAtual.getRegionHeight() * escala;
        game.batch.draw(jogador.frameBarraVidaAtual, 20, altura - 100, barraWidth, barraHeight);

        game.font.getData().setScale(1.25f);
        String objetivo = chaveLiberada
                ? "CHAVE LIBERADA! Encontre a chave para vencer!"
                : "OBJETIVO: Derrote 10 inimigos (" + numKills + "/10)";
        desenharTextoComSombra(objetivo, 20, altura - 125, 1f, 0.85f, 0.25f, 1f);

        // Debug do HUD:
        // game.font.setColor(1, 1, 1, 1);
        // game.font.draw(game.batch, "VIDA: " + jogador.getVida(), 20, altura - 20);
        // game.font.draw(game.batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20,
        // altura - 40);
        // game.font.draw(game.batch, "Kills: " + numKills, 20, altura - 60);
        game.font.getData().setScale(1f);
        game.batch.end();
    }

    // Função pra checar paredes (deixei aqui porque o tiro usa também)
    /*
     * private boolean eParede(float x, float y) {
     * int tileX = (int) (x / tamanhoTile);
     * int tileY = (int) (y / tamanhoTile);
     * if (tileX < 0 || tileX >= larguraMapa || tileY < 0 || tileY >= alturaMapa)
     * return true;
     * return dungeon.getMapa()[tileX][tileY] == 1;
     * }
     */

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
    }

    @Override
    public void show() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        imgChao.dispose();
        imgParede.dispose();
        imgLuz.dispose();
        sheetTiro.dispose();
        imgChave.dispose();
        jogador.dispose();

        for (Inimigo i : listaInimigo) {
            i.dispose();
        }
    }

    private void liberarChaveSeNecessario() {
        if (chaveLiberada || numKills < KILLS_NECESSARIOS) {
            return;
        }

        Rectangle salaChave = dungeon.getSalas().get(dungeon.getSalas().size() - 1);
        chaveX = salaChave.x * tamanhoTile + salaChave.width * tamanhoTile / 2f;
        chaveY = salaChave.y * tamanhoTile + salaChave.height * tamanhoTile / 2f;
        hitboxChave = new java.awt.Rectangle((int) chaveX - 16, (int) chaveY - 16, 32, 32);
        chaveLiberada = true;
    }

    private void desenharTextoComSombra(String texto, float x, float y, float vermelho, float verde,
            float azul, float alfa) {
        game.font.setColor(0f, 0f, 0f, alfa);
        game.font.draw(game.batch, texto, x + 3, y - 3);
        game.font.setColor(vermelho, verde, azul, alfa);
        game.font.draw(game.batch, texto, x, y);
    }
}
