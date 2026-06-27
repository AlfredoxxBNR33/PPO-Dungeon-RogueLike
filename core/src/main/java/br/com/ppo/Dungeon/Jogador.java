package br.com.ppo.Dungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Jogador {

    public static final int BAIXO = 0;
    public static final int CIMA = 1;
    public static final int ESQUERDA = 2;
    public static final int DIREITA = 3;

    public float x, y;
    float velocidade = 150f;
    public int direcaoAtual = BAIXO;
    float tempoAnimacao = 0f;
    public int vida = 25;
    private float tempoInvulnerabilidade = 0f;

    Texture sheetAstronauta;
    Animation<TextureRegion> animBaixo, animCima, animEsq, animDir;
    TextureRegion frameAtual;

    Texture imgBarraVida;
    TextureRegion[] framesBarraVida;
    TextureRegion frameBarraVidaAtual;

    // A nova caixa de colisão do jogador
    public Rectangle hitBox;

    public Jogador(float xInicial, float yInicial) {
        this.x = xInicial;
        this.y = yInicial;

        carregarAnimacao();
        carregarBarraVida();
        // Substituindo a antiga margem de 14 por uma caixa de 28x28 centralizada
        hitBox = new Rectangle(x - 14f, y - 14f, 28, 28);

        // Inicializa o frame atual para evitar renderizar nulo antes do primeiro
        // movimento
        frameAtual = animBaixo.getKeyFrame(0f, true);
    }

    private void carregarBarraVida() {
        imgBarraVida = new Texture("barra_de_vida.png");

        imgBarraVida.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        int frameWidth = imgBarraVida.getWidth();
        int frameHeight = imgBarraVida.getHeight() / 6;

        framesBarraVida = new TextureRegion[6];
        for (int i = 0; i < 6; i++) {
            framesBarraVida[i] = new TextureRegion(imgBarraVida, 0, i * frameHeight, frameWidth, frameHeight);
        }
        frameBarraVidaAtual = framesBarraVida[0];
    }

    private void carregarAnimacao() {

        sheetAstronauta = new Texture("astronauta_anim.png");
        sheetAstronauta.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        TextureRegion[][] tmp = TextureRegion.split(sheetAstronauta, 32, 32);
        animBaixo = new Animation<>(0.15f, tmp[0]);
        animDir = new Animation<>(0.15f, tmp[1]);
        animEsq = new Animation<>(0.15f, tmp[2]);
        animCima = new Animation<>(0.15f, tmp[3]);
    }

    public void update(float dt, DungeonPT2 dungeon, int tamanhoTile, int larguraMapa, int alturaMapa) {
        if (tempoInvulnerabilidade > 0f) {
            tempoInvulnerabilidade -= dt;
        }

        atualizarBarraVida();

        float novaX = x;
        float novaY = y;
        boolean moveu = false;
        Vector2 direcaoMovimento = new Vector2();

        // Calcula a intenção de movimento baseada no teclado
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            direcaoMovimento.y += 1f;
            direcaoAtual = CIMA;
            moveu = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            direcaoMovimento.y -= 1f;
            direcaoAtual = BAIXO;
            moveu = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            direcaoMovimento.x -= 1f;
            direcaoAtual = ESQUERDA;
            moveu = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            direcaoMovimento.x += 1f;
            direcaoAtual = DIREITA;
            moveu = true;
        }

        // Normaliza o vetor para evitar velocidade maior na diagonal
        if (direcaoMovimento.len() > 0) {
            direcaoMovimento.nor().scl(velocidade * dt);
            novaX += direcaoMovimento.x;
            novaY += direcaoMovimento.y;
        }

        // Teste de colisão no eixo X empurrando a caixa para o futuro
        hitBox.setPosition(novaX - 14f, y - 14f);
        if (!GerenciadorColisao.colideComParede(hitBox, dungeon, tamanhoTile, larguraMapa, alturaMapa)) {
            x = novaX;
        }

        // Teste de colisão no eixo Y empurrando a caixa para o futuro
        hitBox.setPosition(x - 14f, novaY - 14f);
        if (!GerenciadorColisao.colideComParede(hitBox, dungeon, tamanhoTile, larguraMapa, alturaMapa)) {
            y = novaY;
        }

        // Garante que a caixa de colisão fique perfeitamente colada no jogador
        hitBox.setPosition(x - 14f, y - 14f);

        if (moveu) {
            tempoAnimacao += dt;
        } else {
            tempoAnimacao = 0;
        }

        if (direcaoAtual == 0)
            frameAtual = animBaixo.getKeyFrame(tempoAnimacao, true);
        else if (direcaoAtual == 1)
            frameAtual = animCima.getKeyFrame(tempoAnimacao, true);
        else if (direcaoAtual == 2)
            frameAtual = animEsq.getKeyFrame(tempoAnimacao, true);
        else if (direcaoAtual == 3)
            frameAtual = animDir.getKeyFrame(tempoAnimacao, true);
    }

    public void render(SpriteBatch batch) {
        batch.draw(frameAtual, x - 16, y - 16);
    }

    private void atualizarBarraVida() {
        int indice = 5;

        if (vida >= 20) {
            indice = 0;
        } else if (vida >= 15) {
            indice = 1;
        } else if (vida >= 10) {
            indice = 2;
        } else if (vida >= 5) {
            indice = 3;
        } else if (vida > 0) {
            indice = 4;
        } else {
            indice = 5;
        }

        frameBarraVidaAtual = framesBarraVida[Math.max(0, Math.min(5, indice))];
    }

    public void dispose() {
        sheetAstronauta.dispose();
        imgBarraVida.dispose();
    }

    public String levarDano(int dano) {
        if (tempoInvulnerabilidade > 0f) {
            return "jogador invulnerável";
        }
        this.vida -= dano;
        tempoInvulnerabilidade = 0.5f;
        return "jogador levou " + dano + " pontos de dano.";
    }

    public String curar(int cura) {
        this.vida += cura;
        return "Jogador curado em " + cura + " pontos.";
    }

    public int getVida() {
        return this.vida;
    }

    // Método útil para quando o inimigo for atacar o jogador
    public Rectangle getHitbox() {
        return hitBox;
    }
}
