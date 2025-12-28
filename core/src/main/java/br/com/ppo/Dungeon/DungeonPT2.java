package br.com.ppo.Dungeon;

/*
 * Essa versão tem como objetivo de fazer salas separadas, sem a existencia de
 * super salas.
 * Fazer um algoritmo que verifica se está dentro de outra sala.
 */

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

public class DungeonPT2 {

    private static Random random = new Random();
    private int largura;
    private int altura;
    private int[][] mapa;
    private ArrayList<Rectangle> salas = new ArrayList<>();

    public DungeonPT2(int largura, int altura) {
        this.largura = largura;
        this.altura = altura;
        this.mapa = new int[largura][altura];
        inicializarMapa();
    }

    public void inicializarMapa() {
        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                mapa[x][y] = 1;
            }
        }
    }

    public void imprimirMapa() {
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                System.out.print(mapa[x][y] == 1 ? "#" : ".");
            }
            System.out.println();
        }
    }

    public Rectangle salaAleatoria() {

        int minTamanho = 10;

        int maxTamanhoLargura = largura / 5;
        int maxTamanhoAltura = altura / 5;

        if (maxTamanhoLargura < minTamanho) {
            maxTamanhoLargura = minTamanho;
        }
        if (maxTamanhoAltura < minTamanho) {
            maxTamanhoAltura = minTamanho;
        }

        int width = minTamanho + random.nextInt((maxTamanhoLargura - minTamanho) + 1);
        int height = minTamanho + random.nextInt((maxTamanhoAltura - minTamanho) + 1);

        int x = random.nextInt(largura - width - 1) + 1;
        int y = random.nextInt(altura - height - 1) + 1;

        return new Rectangle(x, y, width, height);
    }

    public void CriarSala(Rectangle sala) {

        for (int x = sala.x; x < sala.x + sala.width; x++) {
            for (int y = sala.y; y < sala.y + sala.height; y++) {
                if (x >= 0 && x < largura && y >= 0 && y < altura) {
                    mapa[x][y] = 0;
                }
            }
        }
    }

    /*
     * Olha, como posso explicar essa parte de gerar o corredor?
     * Parece ser simples, e é. Porém pra chegar nisso foi um bom quebra cabeça.
     * Bom quandoeu aciono a função salaAleatoria(), ele cria um retangle com
     * tamanhos dentro do limite
     * que estabeleci e joga ele dentro de um ArrayList para facil acesso de
     * dimensões.
     * Ai entra a parte dos corredores, eles percorrem o arraylist conectando a
     * partir do lugar de numero 1 com o de numero 0,
     * seguindo essa sequencia fica +- assim: 1 se conecta com 0, 2 com 1, 3 com
     * 2... e por assim vai até atingir o limite
     * do laço de repetição que mais pra frente determinarei.
     */
    public void criarCorredor() {
        for (int i = 1; i < salas.size(); i++) {
            Rectangle salaAnterior = salas.get(i - 1);
            Rectangle salaAtual = salas.get(i);

            /*
             * E como funciona o ponto de partida do corredor?
             * Aqui foi o quebra cabeça. Acho que lendo ja dá pra ter ideia, mas mesmo assim
             * vou explicar como penso.
             *
             * O inicio do corredor se determinará com a sala anterior a selecionada do
             * array.
             * (se estiver selecionado o obj do lugar 1, vai se conectar com o 0 primeiro [o
             * anterior]. )
             *
             * O método soma o ponto de partida do retangulo (sala) e junta com a largura da
             * mesma dividida por 2.
             * Exemplo: X = 14; width = 16 --> 16/2=8 >>> 14+8=22 (Isso não foi proposital).
             * Isso ocorre também
             * nas outras cordenadas, seja do y final/inicial ou o x final/inicial.
             */
            int xInicial = salaAnterior.x + salaAnterior.width / 2;
            int yInicial = salaAnterior.y + salaAnterior.height / 2;
            int xFinal = salaAtual.x + salaAtual.width / 2;
            int yFinal = salaAtual.y + salaAtual.height / 2;

            // Aqui envio as informações calculadas
            corredorH(xInicial, xFinal, yInicial);
            corredorV(yInicial, yFinal, xFinal);

        }
    }

    // Função de fazer corredor horizontal.
    public void corredorH(int x1, int x2, int yInicial) {
        /*
         * Aqui é simples, vale pra ambas funções (Corredor 'H' e 'V' [Isso da pra fazer
         * mais um trocadilho...]).
         * Dentro do for, eu apenas vou percorrer as coordenas que essa função recebeu.
         * Essas funções da classe Math, são para evitar a troca de ordem do maior pra o
         * menor (ou vice versa), mesmo que seja dificil ocorrer mas é bom se precaver.
         */
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            mapa[x][yInicial] = 0;
        }
    }

    // Função de fazer corredor vertical.
    public void corredorV(int y1, int y2, int xFinal) {
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            mapa[xFinal][y] = 0;
        }
    }

    // Metodo principal de gerar a dungeon.
    public void gerarDungeon(int numeroSalas, int limiteDePaciencia) {
        int tentativasTotais = 0;

        while (salas.size() < numeroSalas && tentativasTotais < limiteDePaciencia) {
            tentativasTotais++;
            Rectangle candidata = salaAleatoria();

            boolean colidiu = false;

            for (Rectangle salaExistente : salas) {

                Rectangle zonaSegura = new Rectangle(
                        salaExistente.x - 2,
                        salaExistente.y - 2,
                        salaExistente.width + 3,
                        salaExistente.height + 3);

                if (candidata.intersects(zonaSegura)) {
                    colidiu = true;
                    break;
                }
            }

            if (!colidiu) {
                salas.add(candidata);
            }
        }
        for (Rectangle salaParaConstruir : salas) {
            CriarSala(salaParaConstruir);
        }
        criarCorredor();
        //imprimirMapa();
        //metodo imprimir mapa desabilitado, nao quero que apareça nada no console. (nao tem pra que...)
    }

    public int[][] getMapa(){
        return mapa;
    }

    public ArrayList<Rectangle> getSalas(){
        return salas;
    }
    public static void main(String[] args) {
        // cuidado no tamanho e do numero de salas.

        DungeonPT2 dungeon = new DungeonPT2(80, 40);
        dungeon.gerarDungeon(15, 1000);

    }

}
