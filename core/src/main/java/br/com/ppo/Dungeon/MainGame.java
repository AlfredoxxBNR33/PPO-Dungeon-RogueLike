package br.com.ppo.Dungeon;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


import java.awt.Rectangle;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MainGame extends ApplicationAdapter {
    SpriteBatch batch;
    OrthographicCamera camera;
    DungeonPT2 dungeon;

    //Texturas do jogo
    Texture imgChao;
    Texture imgParede;
    Texture imgPlayer;
    Texture imgLuz;

    //Configuraçao
    int tamanhoTile = 32;
    int larguraMapa = 100;
    int alturaMapa = 100;
    int numSalas = 20;
    int paciencia = 1000;

    //Variaveis do jogador
    //Float para ser preciso e suave
    float playerX, playerY;

    float velocidade = 150f; //150 pixeis por segundo

    @Override
    public void create(){
        batch = new SpriteBatch();

        //Camera para ver de cima
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800,600);
        // Dá um zoom para ficar parecido com "The Escapists" (quanto menor, mais perto)
        camera.zoom = 0.9f;

        //Carregar as texturas
        imgPlayer = new Texture("astronauta.png");
        imgChao = new Texture("chao.png");
        imgParede= new Texture("parede.png");
        imgLuz = new Texture("luz.png");

        //Filtro para nao deixar a imagem borrada
        imgPlayer.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        imgChao.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        imgParede.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        //Gera a dungeon
        dungeon=new DungeonPT2(larguraMapa,alturaMapa);
        dungeon.gerarDungeon(numSalas,paciencia);

        // O boneco vai aparecer na sala 0, o inicio de toda a geraçao do mapa.
        //Pega a lista das salas que criamos
        Rectangle primeiraSala = dungeon.getSalas().get(0);

        //Agora o boneco anda por pixels, e nao TILES como antes.
        playerX = (primeiraSala.x * tamanhoTile) + (primeiraSala.width * tamanhoTile /2f);
        playerY = (primeiraSala.y * tamanhoTile) + (primeiraSala.height * tamanhoTile /2f);

    }

    @Override
    public void render(){
        //Logica do movimento do boneco
        moverJogador();

        //Pra camera seguir o boneco
        camera.position.set(playerX, playerY, 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        //Limpa a tela
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        int[][] mapa = dungeon.getMapa();

        //Primeira otimização do jogo!!!!
        //Agora vai ser desenhado somente o que está no campo de visão da camera.
        //Isso evita desenhar um monte de TILE e travar algum computador.

        int visao = 20; //Quantidade de blocos para desenhar
        int centrox = (int) (playerX / tamanhoTile);
        int centroy = (int) (playerY / tamanhoTile);

        for(int x = centrox - visao; x < centrox + visao; x++) {
            for (int y = centroy - visao; y < centroy + visao; y++) {
                if (x >= 0 && x < larguraMapa && y >= 0 && y < alturaMapa) {
                    if (mapa[x][y] == 1) {
                        batch.draw(imgParede, x * tamanhoTile, y * tamanhoTile);
                    } else if (mapa[x][y] == 0) {
                        batch.draw(imgChao, x * tamanhoTile, y * tamanhoTile);
                    }
                }
            }
        }
        //Desenha o jogador
        batch.draw(imgPlayer, playerX-16,playerY-16);

        //Desenha a vinheta de sombra
        float larguraLuz = 1000;
        float alturaLuz = 1000;

        batch.draw(imgLuz, playerX - (larguraLuz/2), playerY - (alturaLuz/2), larguraLuz, alturaLuz);

        batch.end();
    }



    //Funçao de mover o jogador
    public void moverJogador(){

        //Logica refeita para ter movimento livre

        //Garante que a velocide seja igual para um pc lento ou rapido
        //Isso deve evitar que fique igual aos GTA's antigos, que ao jogar com um computador novo
        //ele fica extremamente rapido.

        float dt = Gdx.graphics.getDeltaTime();

        // Variáveis temporárias para onde o jogador QUER ir
        float novaX = playerX;
        float novaY = playerY;

        // Note: isKeyPressed (contínuo) em vez de isKeyJustPressed (toque único)
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            novaY += velocidade * dt;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            novaY -= velocidade * dt;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            novaX -= velocidade * dt;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            novaX += velocidade * dt;
        }

        //Verificaçao de colisao
        // Verifica o eixo X primeiro
        if (!colideComParede(novaX, playerY)) {
            playerX = novaX;
        }
        // Depois verifica o eixo Y (isso permite "deslizar" na parede)
        if (!colideComParede(playerX, novaY)) {
            playerY = novaY;
        }
    }


    private boolean colideComParede(float x, float y) {
        // Tamanho da caixa de colisão (um pouco menor que 32 para não travar nas quinas)
        float margem = 14f; // 14 pra cada lado = 28 pixels de largura total

        // Verifica os 4 cantos: Esquerda-Baixo, Direita-Baixo, Esquerda-Cima, Direita-Cima
        if (eParede(x - margem, y - margem)) return true;
        if (eParede(x + margem, y - margem)) return true;
        if (eParede(x - margem, y + margem)) return true;
        if (eParede(x + margem, y + margem)) return true;

        return false; // Se nenhum canto bateu, tá livre
    }

    private boolean eParede (float x, float y){
        // Converte pixel (ex: 1550) para indice do array (ex: 48)
        int tileX = (int) (x / tamanhoTile);
        int tileY = (int) (y / tamanhoTile);

        // Segurança para não sair do mapa
        if (tileX < 0 || tileX >= larguraMapa || tileY < 0 || tileY >= alturaMapa) {
            return true; // Fora do mapa é parede
        }

        // Retorna VERDADEIRO se o bloco for 1 (Parede)
        return dungeon.getMapa()[tileX][tileY] == 1;
    }

    @Override
    public void dispose(){
        batch.dispose();
        imgChao.dispose();
        imgParede.dispose();
        imgPlayer.dispose();
        imgLuz.dispose();
    }

}
