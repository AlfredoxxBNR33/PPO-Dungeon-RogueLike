package br.com.ppo.Dungeon;

import com.badlogic.gdx.math.Rectangle;

public class GerenciadorColisao {

    // Recebe o retângulo de quem está tentando andar e verifica os quatro cantos dele
    public static boolean colideComParede(Rectangle caixa, DungeonPT2 dungeon, int tamanhoTile, int larguraMapa, int alturaMapa) {
        if (eParede(caixa.x, caixa.y, dungeon, tamanhoTile, larguraMapa, alturaMapa)) return true;
        if (eParede(caixa.x + caixa.width, caixa.y, dungeon, tamanhoTile, larguraMapa, alturaMapa)) return true;
        if (eParede(caixa.x, caixa.y + caixa.height, dungeon, tamanhoTile, larguraMapa, alturaMapa)) return true;
        if (eParede(caixa.x + caixa.width, caixa.y + caixa.height, dungeon, tamanhoTile, larguraMapa, alturaMapa)) return true;
        return false;
    }

    // A matemática pura que lê a matriz do mapa
    private static boolean eParede(float testeX, float testeY, DungeonPT2 dungeon, int tamanhoTile, int larguraMapa, int alturaMapa) {
        int tileX = (int) (testeX / tamanhoTile);
        int tileY = (int) (testeY / tamanhoTile);

        if (tileX < 0 || tileX >= larguraMapa || tileY < 0 || tileY >= alturaMapa) return true;

        return dungeon.getMapa()[tileX][tileY] == 1;
    }
}

