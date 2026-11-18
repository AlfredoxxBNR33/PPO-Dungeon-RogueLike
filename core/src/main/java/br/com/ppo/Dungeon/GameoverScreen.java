package br.com.ppo.Dungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class GameoverScreen implements Screen {

    private final MainGame game;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final Texture imagemGameOver;
    private final Rectangle botaoReiniciar;
    private final Rectangle botaoSair;
    private final Texture imagemBotaoJogar;
    private final Texture imagemBotaoSair;
    private final Vector3 toqueMouse = new Vector3();

    public GameoverScreen(final MainGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);
        batch = new SpriteBatch();
        imagemGameOver = new Texture("gameover.png");
        imagemBotaoJogar = new Texture("botao_jogar.png");
        imagemBotaoSair = new Texture("botao_sair.png");
        botaoReiniciar = new Rectangle();
        botaoSair = new Rectangle();
        atualizarBotoes();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(imagemGameOver, 0, 0, camera.viewportWidth, camera.viewportHeight);
        batch.draw(imagemBotaoJogar, botaoReiniciar.x, botaoReiniciar.y, botaoReiniciar.width,
                botaoReiniciar.height);
        batch.draw(imagemBotaoSair, botaoSair.x, botaoSair.y, botaoSair.width, botaoSair.height);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            dispose();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            reiniciarJogo();
        } else if (Gdx.input.justTouched()) {
            toqueMouse.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(toqueMouse);
            if (botaoReiniciar.contains(toqueMouse.x, toqueMouse.y)) {
                reiniciarJogo();
            } else if (botaoSair.contains(toqueMouse.x, toqueMouse.y)) {
                dispose();
                Gdx.app.exit();
            }
        }
    }

    private void atualizarBotoes() {
        float y = 20;
        botaoReiniciar.set(camera.viewportWidth / 2f - 285, y, 260, 130);
        botaoSair.set(camera.viewportWidth / 2f + 25, y, 260, 130);
    }

    private void reiniciarJogo() {
        game.setScreen(new GameScreen(game));
        dispose();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        atualizarBotoes();
    }

    @Override
    public void show() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        imagemGameOver.dispose();
        imagemBotaoJogar.dispose();
        imagemBotaoSair.dispose();
    }
}
