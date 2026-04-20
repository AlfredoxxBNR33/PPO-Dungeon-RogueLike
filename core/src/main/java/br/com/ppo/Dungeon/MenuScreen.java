package br.com.ppo.Dungeon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;



public class MenuScreen implements  Screen{

    final MainGame jogo;
    OrthographicCamera camera;
    private SpriteBatch batch;

    Texture imgFundo, imgLogo, imgBotao;

    Rectangle hitboxBotao;
    Vector3 toqueMouse;

    public MenuScreen (final MainGame jogobase){
        this.jogo=jogobase;

        camera = new OrthographicCamera();
        camera.setToOrtho(false , 1980, 1080);

        batch = new SpriteBatch();

        /*
        texturas para carregar:
         */
        imgFundo = new Texture("fundo_menu.png");
        imgLogo = new Texture("logo.png");
        imgBotao = new Texture("botao_jogar.png");




        hitboxBotao = new Rectangle(800 / 2 - 100, 150, 200, 80);
        toqueMouse = new Vector3();
    }

    @Override
    public void render (float delta){
        ScreenUtils.clear(0,0,0,1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // 1. Desenha o fundo preenchendo a tela
        batch.draw(imgFundo, 0, 0, 800, 600);

        // 2. Desenha a logo na parte superior
        batch.draw(imgLogo, 800 / 2f - 200, 350, 400, 200);

        // 3. Desenha o botão de jogar
        batch.draw(imgBotao, hitboxBotao.x, hitboxBotao.y, hitboxBotao.width, hitboxBotao.height);
        batch.end();

        // Lógica do Clique
        if (Gdx.input.justTouched()) {
            toqueMouse.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(toqueMouse);

            if (hitboxBotao.contains(toqueMouse.x, toqueMouse.y)) {
                // Comando para trocar para a tela do jogo real
                jogo.setScreen(new GameScreen(jogo));
                dispose();
            }
        }
    }
    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        // Muito importante limpar a memória ao sair do menu
        batch.dispose();
        imgFundo.dispose();
        imgLogo.dispose();
        imgBotao.dispose();
    }
}
