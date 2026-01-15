package br.com.ppo.Dungeon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MainGame extends Game {

    // Essas variáveis são públicas para que qualquer tela possa acessar
    public SpriteBatch batch;
    public BitmapFont font; // A fonte para escrever na tela (HUD)

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Carrega a fonte padrão do sistema (depois podemos colocar uma pixelada)
        font = new BitmapFont();

        // Aqui dizemos: "Gerente, comece o trabalho pela tela do Jogo"
        // No futuro, mudaremos para: setScreen(new MenuScreen(this));
        this.setScreen(new GameScreen(this));
    }

    @Override
    public void render() {
        // O Game.render() delega o desenho para a tela atual (GameScreen, MenuScreen, etc)
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
