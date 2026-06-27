package br.com.ppo.Dungeon;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.ArrayList;
import java.util.Random;

public class Inimigo {

    public static final int BAIXO = 0;
    public static final int CIMA = 1;
    public static final int ESQUERDA = 2;
    public static final int DIREITA = 3;

    public float x, y;
    public float velocidade = 50f;
    public int vida = 3;
    public boolean deveRemover = false;

    // Distância mínima para o inimigo parar de se aproximar do jogador
    float distanciaParada = 6f; // renomeado de distancia_parada (anteriormente 40f)
    // Distância na qual o inimigo detecta/parte para perseguir o jogador
    float distanciaDeteccao = 220f; // mantido nome (detecção)

    // Patrulha quando jogador estiver fora da detecção
    Vector2 direcaoPatrulha = new Vector2(); // renomeado de wanderDir
    float tempoPatrulha = 0f; // renomeado de wanderTimer
    float intervaloPatrulhaMinimo = 1.5f; // renomeado de wanderIntervalMin
    float intervaloPatrulhaMaximo = 3.5f; // renomeado de wanderIntervalMax
    Random aleatorio = new Random(); // renomeado de random

    // Inicializaçao das variaveis da textura
    Texture imgInimigo;
    Animation<TextureRegion> animBaixo, animCima, animEsq, animDir;
    TextureRegion frameAtual;
    float tempoAnimacao = 0f;
    public int direcaoAtual = BAIXO;
    boolean moveu = false;
    Texture morteInimigo;
    Animation<TextureRegion> animMorte;
    TextureRegion frameMorteAtual;
    float tempoAnimacaoMorte = 0f;
    boolean morto = false;

    public Rectangle caixaColisao; // renomeado de hitBox

    public Inimigo(float xInicial, float yInicial) { // Contrutor
        this.x = xInicial;
        this.y = yInicial;

        carregarAnimacao();
        carregarAnimacaoMorte();

        // caixa de colisão centrada no ponto x,y; o sprite é desenhado com offset
        // -16,-16
        // Comentário: corrigimos a posição da caixa de colisão para ficar centrada
        // no ponto (x,y) do inimigo, evitando desalinhamento com o sprite.
        caixaColisao = new Rectangle(x - 12f, y - 12f, 24, 24);
    }

    private void carregarAnimacaoMorte() {
        try {
            morteInimigo = new Texture("inimigo_morte_anim.png");
        } catch (GdxRuntimeException e) {
            morteInimigo = imgInimigo != null ? imgInimigo : new Texture("alien.png");
        }

        morteInimigo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[][] tmp = TextureRegion.split(morteInimigo, 32, 32);
        animMorte = new Animation<>(0.15f, tmp[0]);
        frameMorteAtual = animMorte.getKeyFrame(0f, false);
    }

    private void carregarAnimacao() {
        try {
            imgInimigo = new Texture("inimigo_anim.png");
        } catch (GdxRuntimeException e) {
            imgInimigo = new Texture("alien.png");
        }

        imgInimigo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[][] tmp = TextureRegion.split(imgInimigo, 32, 32);
        animBaixo = new Animation<>(0.15f, tmp[0]);
        animEsq = new Animation<>(0.15f, tmp[1]);
        animDir = new Animation<>(0.15f, tmp[2]);
        animCima = new Animation<>(0.15f, tmp[3]);
        frameAtual = animBaixo.getKeyFrame(0f, true);
    }

    public void morrer() {
        iniciarMorte();
    }

    private void iniciarMorte() {
        if (morto) {
            return;
        }
        morto = true;
        tempoAnimacaoMorte = 0f;
        frameMorteAtual = animMorte.getKeyFrame(0f, false);
        deveRemover = false;
    }

    public void update(float dt, float alvoX, float alvoY, DungeonPT2 dungeon, int tamanhoTile, int larguraMapa,
            int alturaMapa, ArrayList<Inimigo> listaInimigos) {

        if (vida <= 0) {
            iniciarMorte();
        }

        if (morto) {
            tempoAnimacaoMorte += dt;
            frameMorteAtual = animMorte.getKeyFrame(tempoAnimacaoMorte, false);
            if (animMorte.isAnimationFinished(tempoAnimacaoMorte)) {
                deveRemover = true;
            }
            return;
        }

        Vector2 posInimigo = new Vector2(x, y);
        Vector2 posJogador = new Vector2(alvoX, alvoY);
        float distancia = posInimigo.dst(posJogador);

        Vector2 movimento = new Vector2();
        moveu = false;

        if (distancia < distanciaDeteccao) {
            // Persegue o jogador até uma distância de parada
            // Comentário: calculamos o vetor direção do jogador em relação ao inimigo,
            // normalizamos (para evitar velocidade maior na diagonal) e multiplicamos
            // por velocidade*dt. Isso garante movimento com velocidade constante.
            if (distancia > distanciaParada) {
                movimento.set(posJogador.sub(posInimigo)).nor().scl(velocidade * dt);
                moveu = true;
            }
        } else {
            // Patrulha simples (wander)
            // Comentário: quando o jogador está fora da detecção, o inimigo escolhe
            // uma direção aleatória periodicamente e anda mais devagar — isso dá
            // a impressão de patrulha/vida própria.
            tempoPatrulha -= dt;
            if (tempoPatrulha <= 0f) {
                // nova direção aleatória
                float ang = (float) (aleatorio.nextFloat() * Math.PI * 2f);
                direcaoPatrulha.set((float) Math.cos(ang), (float) Math.sin(ang));
                tempoPatrulha = intervaloPatrulhaMinimo + aleatorio.nextFloat()
                        * (intervaloPatrulhaMaximo - intervaloPatrulhaMinimo);
            }
            movimento.set(direcaoPatrulha).nor().scl(velocidade * 0.5f * dt);
            moveu = true;
        }

        if (moveu && movimento.len() > 0f) {
            if (Math.abs(movimento.x) > Math.abs(movimento.y)) {
                direcaoAtual = movimento.x >= 0f ? DIREITA : ESQUERDA;
            } else {
                direcaoAtual = movimento.y >= 0f ? CIMA : BAIXO;
            }
            tempoAnimacao += dt;
        } else {
            tempoAnimacao = 0f;
        }

        if (direcaoAtual == BAIXO) {
            frameAtual = animBaixo.getKeyFrame(tempoAnimacao, true);
        } else if (direcaoAtual == CIMA) {
            frameAtual = animCima.getKeyFrame(tempoAnimacao, true);
        } else if (direcaoAtual == ESQUERDA) {
            frameAtual = animEsq.getKeyFrame(tempoAnimacao, true);
        } else if (direcaoAtual == DIREITA) {
            frameAtual = animDir.getKeyFrame(tempoAnimacao, true);
        }

        // Tentativa de movimentação com checagem de colisão separada para X e Y
        // Tentativa de movimentação com checagem de colisão separada para X e Y
        // Comentário: movemos primeiro no eixo X e depois no Y, cada um com checagem
        // de colisão. Isso evita que colisões diagonais empurrem o inimigo para dentro
        // de paredes e dá um comportamento mais robusto de navegação.
        float novaX = x + movimento.x;
        float novaY = y; // primeiro X
        caixaColisao.setPosition(novaX - 12f, novaY - 12f);
        if (!GerenciadorColisao.colideComParede(caixaColisao, dungeon, tamanhoTile, larguraMapa, alturaMapa)
                && !GerenciadorColisao.colideComOutroInimigo(caixaColisao, listaInimigos, this)) {
            x = novaX;
        }

        novaX = x;
        novaY = y + movimento.y; // depois Y
        caixaColisao.setPosition(novaX - 12f, novaY - 12f);
        if (!GerenciadorColisao.colideComParede(caixaColisao, dungeon, tamanhoTile, larguraMapa, alturaMapa)
                && !GerenciadorColisao.colideComOutroInimigo(caixaColisao, listaInimigos, this)) {
            y = novaY;
        }

        // Garantir caixa de colisão alinhada corretamente
        caixaColisao.setPosition(x - 12f, y - 12f);

    }

    public void render(SpriteBatch batch) {
        if (morto) {
            batch.draw(frameMorteAtual, x - 16, y - 16);
        } else {
            batch.draw(frameAtual, x - 16, y - 16);
        }
    }

    public void dispose() {
        imgInimigo.dispose();
        morteInimigo.dispose();
    }

    public Rectangle getHitbox() {
        // Mantive o nome do método para compatibilidade com o restante do código
        // (poderíamos renomear para getCaixaColisao(), mas isso exigiria alterações
        // em outras classes). Retorna a `caixaColisao` renomeada.
        return caixaColisao;
    }

    public void darDano(int dano) {
        this.vida -= dano;
        if (this.vida <= 0) {
            iniciarMorte();
        }
    }

    public int getVida() {
        return this.vida;
    }
}
