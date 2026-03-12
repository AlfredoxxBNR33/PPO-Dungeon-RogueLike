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

        hitBox= new Rectangle(x, y, 32, 32);
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

            if (!colideComParede(novaX, y, dungeon, tamanhoTile, larguraMapa, alturaMapa)) x = novaX;
            if (!colideComParede(x, novaY, dungeon, tamanhoTile, larguraMapa, alturaMapa)) y = novaY;
        }
        hitBox.setPosition(x,y);

        if(vida<=0){
            deveRemover=true;
        }
    }

    public void render(SpriteBatch batch){
        batch.draw(imgInimigo,x-16,y-16);
    }

    private boolean colideComParede(float testeX,float testeY, DungeonPT2 dungeon, int tamanhoTile, int larguraMapa, int alturaMapa){
        float margem = 4f;
        if(eParede(testeX+margem, testeY+margem, dungeon, tamanhoTile,larguraMapa,alturaMapa )){
            return true;
        }if(eParede (testeX+32 - margem, testeY+margem, dungeon, tamanhoTile,larguraMapa,alturaMapa )){
            return true;
        }if(eParede(testeX+margem, testeY + 32 - margem, dungeon, tamanhoTile,larguraMapa,alturaMapa)){
            return true;
        }if(eParede(testeX+margem, testeY +32 - margem, dungeon, tamanhoTile, larguraMapa, alturaMapa)){
            return true;
        }
        return false;
    }

    private boolean eParede(float testeX,float testeY, DungeonPT2 dungeon, int tamanhoTile, int larguraMapa, int alturaMapa){
        int tileX = (int) (testeX/tamanhoTile);
        int tileY = (int) (testeY/tamanhoTile);
        if(tileX < 0 || tileX >= larguraMapa || tileY < 0 || tileY >=alturaMapa){
            return true;
        }
        return  dungeon.getMapa()[tileX][tileY]==1;
    }


    public void dispose(){
        imgInimigo.dispose();
    }
}
