package br.com.ppo.Dungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Jogador {

    public float x, y;
    float velocidade = 150f;
    public int direcaoAtual = 0;
    float tempoAnimacao = 0f;
    public int vida = 100;

    Texture sheetAstronauta;
    Animation<TextureRegion> animBaixo, animCima, animEsq, animDir;
    TextureRegion frameAtual;

    // A nova caixa de colisão do jogador
    public Rectangle hitBox;

    public Jogador (float xInicial, float yInicial){
        this.x = xInicial;
        this.y = yInicial;

        sheetAstronauta = new Texture("astronauta_anim.png");
        sheetAstronauta.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        TextureRegion[][] tmp = TextureRegion.split(sheetAstronauta, 32, 32);
        animBaixo = new Animation<>(0.15f, tmp[0]);
        animDir   = new Animation<>(0.15f, tmp[1]);
        animEsq   = new Animation<>(0.15f, tmp[2]);
        animCima  = new Animation<>(0.15f, tmp[3]);

        // Substituindo a antiga margem de 14 por uma caixa de 28x28 centralizada
        hitBox = new Rectangle(x - 14f, y - 14f, 28, 28);
    }

    public void update (float dt, DungeonPT2 dungeon, int tamanhoTile, int larguraMapa, int alturaMapa){
        float novaX = x;
        float novaY = y;
        boolean moveu = false;

        // Calcula a intenção de movimento baseada no teclado
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) { novaY += velocidade * dt; direcaoAtual = 1; moveu = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) { novaY -= velocidade * dt; direcaoAtual = 0; moveu = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) { novaX -= velocidade * dt; direcaoAtual = 2; moveu = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) { novaX += velocidade * dt; direcaoAtual = 3; moveu = true; }

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

        if (direcaoAtual == 0) frameAtual = animBaixo.getKeyFrame(tempoAnimacao, true);
        else if (direcaoAtual == 1) frameAtual = animCima.getKeyFrame(tempoAnimacao, true);
        else if (direcaoAtual == 2) frameAtual = animEsq.getKeyFrame(tempoAnimacao, true);
        else if (direcaoAtual == 3) frameAtual = animDir.getKeyFrame(tempoAnimacao, true);
    }

    public void render(SpriteBatch batch) {
        batch.draw(frameAtual, x - 16, y - 16);
    }

    public void dispose() {
        sheetAstronauta.dispose();
    }

    // Método útil para quando o inimigo for atacar o jogador
    public Rectangle getHitbox() {
        return hitBox;
    }
}
