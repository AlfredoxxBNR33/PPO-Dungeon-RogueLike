package br.com.ppo.Dungeon;


import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;


public class Tiro {

    // posição e velocidade
    float x, y;
    float velocidade = 250f; // mais rapido que o player
    float velX, velY; // direçao de movimento
    float rotacao; // define a rotação da animação de tiro. Os valores serão definidos em GRAU.



    //Recebida de fora para economizar memoria
    Animation<TextureRegion> animacao;
    float tempodeVida = 0;

    // Controle de vida útil (para a bala não voar pra sempre e pesar o jogo)
    boolean deveRemover = false;
    float tempoMaximo = 1.5f; // 3 segundos antes de sumir sozinho

    Rectangle retanguloColisao;

    public Tiro(float xInicial, float yInicial, int direcao,Animation<TextureRegion> animRecebida) {

        this.x = xInicial;
        this.y = yInicial;
        this.animacao = animRecebida;
        this.retanguloColisao = new Rectangle(x, y, 16, 16); // Tamanho pequeno (8x8)

        // Define a direção com base no input do Player (0=Baixo, 1=Cima, 2=Esq, 3=Dir)
        // Isso aqui deixa modular: se o player mudar a lógica de direção, o tiro se adapta
        switch (direcao) {
            case 0:
                velX = 0;
                velY = -1;
                rotacao= 270;
                break; // Baixo
            case 1:
                velX = 0;
                velY = 1;
                rotacao= 90;
                break;  // Cima
            case 2:
                velX = -1;
                velY = 0;
                rotacao = 180;
                break; // Esquerda
            case 3:
                velX = 1;
                velY = 0;
                rotacao = 0;
                break;  // Direita
        }
    }

    public void update (float dt,DungeonPT2 dungeon, int tamanhoTile, int larguraMapa, int alturaMapa){

        // A física básica: Posição = Posição Anterior + (Velocidade * Tempo)
        x+= velX * velocidade * dt;
        y+= velY * velocidade * dt;

        //Atualiza a caixa de colisão para acompanhar o desenho
        retanguloColisao.setPosition(x, y);

        tempodeVida += dt;

        //Contagem regressiva para sumir
        tempoMaximo -=dt;
        if(GerenciadorColisao.colideComParede(retanguloColisao, dungeon, tamanhoTile, larguraMapa, alturaMapa) || tempoMaximo <= 0){
            deveRemover=true;
        }
    }
    public void render (SpriteBatch batch){
        TextureRegion frameAtual = animacao.getKeyFrame(tempodeVida, true);

        // O 16, 16 é o centro do giro (metade do tamanho 32x32)
        // O 32, 32 é o tamanho final da imagem na tela
        // O 1, 1 é a escala (sem zoom)
        batch.draw(frameAtual, x, y, 16, 16, 32, 32, 1, 1, rotacao);
    }
}
