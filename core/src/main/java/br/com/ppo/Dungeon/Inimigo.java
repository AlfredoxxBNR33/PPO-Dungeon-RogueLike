package br.com.ppo.Dungeon;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Inimigo {

    public float x,y;
    public float velocidade = 70f;
    public int vida = 3;
    public boolean deveRemover = false;


    // Distancia pessoal do inimigo
    float distancia_parada = 40f;

    Texture imgInimigo;
    public Rectangle hitBox;


    public Inimigo (float xInicial,float yInicial){
        this.x = xInicial;
        this.y = yInicial;

        imgInimigo = new Texture("alien.png");

        hitBox= new Rectangle(x+12f, y+12f, 24, 24);
    }

    public void update (float dt, float alvoX, float alvoY, DungeonPT2 dungeon, int tamanhoTile, int larguraMapa, int alturaMapa){

        Vector2 posInimigo = new Vector2 (x,y);
        Vector2 posJogador = new Vector2(alvoX,alvoY);
        float distancia = posInimigo.dst(posJogador);

        if(distancia > distancia_parada) {
            float novaX = x;
            float novaY = y;

            if (alvoX > x) {
                novaX += velocidade * dt;
            }
            if (alvoX < x) {
                novaX -= velocidade * dt;
            }
            if (alvoY > y) {
                novaY += velocidade * dt;
            }
            if (alvoY < y) {
                novaY -= velocidade * dt;
            }
            hitBox.setPosition(novaX + 12f, y + 12f);
            if (!GerenciadorColisao.colideComParede(hitBox, dungeon, tamanhoTile, larguraMapa, alturaMapa)) {
                x = novaX; // Se estiver livre, o inimigo dá o passo real
            }

            hitBox.setPosition(x + 12f, novaY + 12f);
            if (!GerenciadorColisao.colideComParede(hitBox, dungeon, tamanhoTile, larguraMapa, alturaMapa)) {
                y = novaY;
            }
        }
        hitBox.setPosition(x-12f,y-12f);

        if(vida<=0){
            deveRemover=true;
        }
    }

    public void render(SpriteBatch batch){
        batch.draw(imgInimigo,x-16,y-16);
    }

    public void dispose(){
        imgInimigo.dispose();
    }

    public Rectangle getHitbox(){
        return hitBox;
    }

    public void darDano(int dano){
        this.vida -= dano;
    }
}
