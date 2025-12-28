package br.com.ppo.Dungeon;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.awt.*;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MainGame extends ApplicationAdapter {
    ShapeRenderer shapeRenderer;
    OrthographicCamera camera;
    DungeonPT2 dungeon;

    //Configuraçao
    int tamanhoTile = 20;
    int larguraMapa = 250;
    int alturaMapa = 250;
    //Variaveis do jogador
    int playerX, playerY;

    @Override
    public void create(){
        shapeRenderer = new ShapeRenderer();

        //Camera para ver de cima
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800,600);

        //Gera a dungeon
        int largura = 250;
        int altura = 250;
        dungeon=new DungeonPT2(larguraMapa,alturaMapa);
        dungeon.gerarDungeon(40,1000);

        // O boneco vai aparecer na sala 0, o inicio de toda a geraçao do mapa.
        //Pega a lista das salas que criamos
        Rectangle primeiraSala = dungeon.getSalas().get(0);

        //Coloca ele bem no centro
        // (x + metade da largura)
        playerX = primeiraSala.x + (primeiraSala.width / 2);
        playerY = primeiraSala.y + (primeiraSala.height / 2);
    }

    @Override
    public void render(){
        //Logica do movimento do boneco
        moverJogador();

        //Limpa a tela
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // A câmera foca na posição X e Y do jogador (convertido para pixels)
        camera.position.set(playerX * tamanhoTile, playerY * tamanhoTile, 0);
        // Dá um zoom para ficar parecido com "The Escapists" (quanto menor, mais perto)
        camera.zoom = 0.5f;

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        //2. Desenha o mapa
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        int[][] mapa = dungeon.getMapa();

        for(int x = 0; x < larguraMapa;x++){
            for(int y = 0; y < alturaMapa;y++){
                if(mapa[x][y]==1){
                    //Quando for parede (1) pinta de cinza
                    shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1);
                }else{
                    //Quando for chao (0) pinta de cinza claro
                    shapeRenderer.setColor(0.8f, 0.8f, 0.8f, 1);
                }

                shapeRenderer.rect(x*tamanhoTile, y*tamanhoTile,tamanhoTile,tamanhoTile);
            }
        }

        // Desenha o Jogador (Quadrado Vermelho)
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(playerX * tamanhoTile, playerY * tamanhoTile, tamanhoTile, tamanhoTile);

        shapeRenderer.end();
    }

    @Override
    public void dispose(){
        shapeRenderer.dispose();
    }

    //Funçao de mover o jogador
    public void moverJogador(){

        int[][] mapa = dungeon.getMapa();

        // Usamos isKeyJustPressed para ele andar 1 bloco por vez (sem deslizar)
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            // Verifica se a coordenada acima NÃO É parede (1)
            if (playerY + 1 < alturaMapa && mapa[playerX][playerY + 1] != 1) {
                playerY++;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            if (playerY - 1 >= 0 && mapa[playerX][playerY - 1] != 1) {
                playerY--;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (playerX - 1 >= 0 && mapa[playerX - 1][playerY] != 1) {
                playerX--;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (playerX + 1 < larguraMapa && mapa[playerX + 1][playerY] != 1) {
                playerX++;
            }
        }
    }
}
