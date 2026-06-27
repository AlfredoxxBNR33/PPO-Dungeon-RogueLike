package br.com.ppo.Dungeon;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Tiro {

    public static final int BAIXO = 0;
    public static final int CIMA = 1;
    public static final int ESQUERDA = 2;
    public static final int DIREITA = 3;

    // posição e velocidade
    float x, y;
    float velocidade = 250f; // mais rapido que o player
    float velX, velY; // direçao de movimento normalizada
    float rotacao; // define a rotação da animação de tiro. Os valores serão definidos em GRAU.

    // Recebida de fora para economizar memória
    Animation<TextureRegion> animacao;
    float tempoVida = 0; // renomeado de tempodeVida para deixar claro que mede vida do tiro

    // Controle de vida útil (para a bala não voar pra sempre e pesar o jogo)
    boolean deveRemover = false;
    float tempoMaximo = 1.5f; // 1.5 segundos antes de sumir sozinho

    Rectangle retanguloColisao;

    public Tiro(float xInicial, float yInicial, int direcao, Animation<TextureRegion> animRecebida) {

        this.x = xInicial;
        this.y = yInicial;
        this.animacao = animRecebida;
        this.retanguloColisao = new Rectangle(x, y, 16, 16); // tamanho real 16x16, ajusta a colisão ao tiro

        // Define a direção com base no input do Player usando constantes
        // Isso deixa o código mais legível e evita magia de números.
        switch (direcao) {
            case BAIXO:
                velX = 0;
                velY = -1;
                rotacao = 270;
                break;
            case CIMA:
                velX = 0;
                velY = 1;
                rotacao = 90;
                break;
            case ESQUERDA:
                velX = -1;
                velY = 0;
                rotacao = 180;
                break;
            case DIREITA:
                velX = 1;
                velY = 0;
                rotacao = 0;
                break;
            default:
                velX = 0;
                velY = 0;
                rotacao = 0;
                break;
        }
    }

    public void update(float dt, DungeonPT2 dungeon, int tamanhoTile, int larguraMapa, int alturaMapa) {

        // A física básica: Posição = Posição Anterior + (Velocidade * Tempo)
        x += velX * velocidade * dt;
        y += velY * velocidade * dt;

        // Atualiza a caixa de colisão para acompanhar o desenho
        retanguloColisao.setPosition(x, y);

        tempoVida += dt;

        // Contagem regressiva para sumir
        tempoMaximo -= dt;
        if (GerenciadorColisao.colideComParede(retanguloColisao, dungeon, tamanhoTile, larguraMapa, alturaMapa)
                || tempoMaximo <= 0) {
            deveRemover = true;
        }
    }

    public void render(SpriteBatch batch) {
        TextureRegion frameAtual = animacao.getKeyFrame(tempoVida, true);

        // O 16, 16 é o centro do giro (metade do tamanho 32x32)
        // O 32, 32 é o tamanho final da imagem na tela
        // O 1, 1 é a escala (sem zoom)
        batch.draw(frameAtual, x, y, 16, 16, 32, 32, 1, 1, rotacao);
    }
}
