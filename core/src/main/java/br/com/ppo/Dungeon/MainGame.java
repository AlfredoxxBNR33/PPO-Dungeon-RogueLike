package br.com.ppo.Dungeon;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


import java.awt.Rectangle;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MainGame extends ApplicationAdapter {
    SpriteBatch batch;
    OrthographicCamera camera;
    DungeonPT2 dungeon;

    //Texturas do jogo
    Texture imgChao, imgParede,  imgPlayer, imgLuz;

    //Variaveis de animação
    Texture sheetPlayer;
    Animation<TextureRegion> animBaixo,animCima,animEsquerda, animDireita;
    TextureRegion frameatual;

    //Qual lado esta olhando
    // 0 = baixo ; 1 = dir ; 2 = esq ; 3 = cima
    int dir_atual = 0; // A padrao é 0

    float temp_anim = 0f;

    //Configuraçao
    int tamanhoTile = 32;
    int larguraMapa = 100;
    int alturaMapa = 100;
    int numSalas = 20;
    int paciencia = 1000;

    //Variaveis do jogador
    //Float para ser preciso e suave
    float playerX, playerY;

    float velocidade = 120f; //150 pixeis por segundo

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

        //Carregar a sprite sheet
        sheetPlayer = new Texture("astronauta_anim.png");


        //Filtro para nao deixar a imagem borrada
        sheetPlayer.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        imgPlayer.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        imgChao.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        imgParede.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Divide a imagem grande em pedacinhos de 32x32
        // 'tmp' é uma matriz: tmp[LINHA][COLUNA]
        TextureRegion[][] tmp = TextureRegion.split(sheetPlayer, 32, 32);

        //Configura as animações (0.15f é a velocidade, quanto menor mais rapido)
        // O numero do array tmp é em relaçao a linha de animação da sprite sheet
        animBaixo = new Animation<>(0.1f, tmp[0]);
        animDireita = new Animation<>(0.1f, tmp[1]);
        animEsquerda = new Animation<>(0.1f, tmp[2]);
        animCima = new Animation<>(0.1f, tmp[3]);

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
        boolean estaAndando= moverJogador();

        //Pra camera seguir o boneco
        camera.position.set(playerX, playerY, 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        //Limpa a tela
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Logica de escolher o frame
        if(estaAndando){
            temp_anim += Gdx.graphics.getDeltaTime();
        }else{
            temp_anim = 0;
        }
        // Escolhe qual animação usar baseado na última direção
        if(dir_atual == 0){
            frameatual = animBaixo.getKeyFrame(temp_anim, true);
        }else if(dir_atual == 1){
            frameatual = animDireita.getKeyFrame(temp_anim, true);
        }else if(dir_atual == 2){
            frameatual = animEsquerda.getKeyFrame(temp_anim, true);
        }else if(dir_atual == 3){
            frameatual = animCima.getKeyFrame(temp_anim, true);
        }

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
        batch.draw(frameatual, playerX-16,playerY-16);

        //Desenha a vinheta de sombra
        float larguraLuz = 1000;
        float alturaLuz = 1000;

        batch.draw(imgLuz, playerX - (larguraLuz/2), playerY - (alturaLuz/2), larguraLuz, alturaLuz);

        batch.end();
    }



    //Funçao de mover o jogador
    public Boolean moverJogador(){

        //Logica refeita para ter movimento livre

        //Garante que a velocide seja igual para um pc lento ou rapido
        //Isso deve evitar que fique igual aos GTA's antigos, que ao jogar com um computador novo
        //ele fica extremamente rapido.

        float dt = Gdx.graphics.getDeltaTime();

        // Variáveis temporárias para onde o jogador QUER ir
        float novaX = playerX;
        float novaY = playerY;

        // Variavel para saber se o boneco mexeu.
        boolean andou = false;

        // Note: isKeyPressed (contínuo) em vez de isKeyJustPressed (toque único)
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            novaY += velocidade * dt;
            dir_atual = 3;
            andou = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            novaY -= velocidade * dt;
            dir_atual = 0;
            andou = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            novaX -= velocidade * dt;
            dir_atual = 2;
            andou = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            novaX += velocidade * dt;
            dir_atual = 1;
            andou = true;
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

        return andou;

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
