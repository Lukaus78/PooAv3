import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SpaceInvadersGame extends JPanel implements ActionListener, KeyListener {
    private static final int LARGURA_TELA = 800;
    private static final int ALTURA_TELA = 600;
    private static final int LARGURA_JOGADOR = 50;
    private static final int ALTURA_JOGADOR = 30;
    private static final int LARGURA_PROJETIL = 4;
    private static final int ALTURA_PROJETIL = 10;
    private static final int LARGURA_INIMIGO = 30;
    private static final int ALTURA_INIMIGO = 30;
    private static final int VELOCIDADE_JOGADOR = 5;
    private static final int VELOCIDADE_PROJETIL = 5;
    private static final int VELOCIDADE_INIMIGO = 2;
    private static final int VELOCIDADE_QUEDA_INIMIGO = 5;
    private static final int INTERVALO_SPAWN_INIMIGO = 100;
    private static final int PONTUACAO_MAXIMA = 500;
    private static final String NOME_JOGO = "Space Invaders";

    private Player jogador;
    private List<Inimigo> inimigos;
    private List<Projetil> projeteis;
    private Timer temporizador;
    private Image imagemFundo;
    private Image imagemJogador;
    private Image imagemInimigo;
    private boolean jogoEmAndamento = false;
    private boolean jogoPausado = false;
    private boolean jogoVencido = false;
    private boolean jogoIniciado = false;
    private int pontuacao = 0;
    private int vidas = 3;
    private boolean moverEsquerda;
    private boolean moverDireita;
    private boolean moverCima;
    private boolean moverBaixo;
    private String nomeJogador;
    private List<Pontuacao> tabelaClassificacao;

    public SpaceInvadersGame() {
        jogador = new Player();
        inimigos = new ArrayList<>();
        projeteis = new ArrayList<>();
        temporizador = new Timer(1000 / 60, this);

        try {
            imagemFundo = ImageIO.read(getClass().getResource("fundo.png"));
            imagemJogador = ImageIO.read(getClass().getResource("nave.png"));
            imagemInimigo = ImageIO.read(getClass().getResource("enemy.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        nomeJogador = JOptionPane.showInputDialog("Digite o nome do jogador:");
        tabelaClassificacao = new ArrayList<>();

        addKeyListener(this);
        setFocusable(true);
    }

    private void iniciarJogo() {
        jogoIniciado = true;
        jogoEmAndamento = true;
        requestFocusInWindow();
        temporizador.start();
        reiniciarJogo();
    }

    private void reiniciarJogo() {
        jogoEmAndamento = true;
        jogoPausado = false;
        jogoVencido = false;
        jogador.reset();
        inimigos.clear();
        projeteis.clear();
        pontuacao = 0;
        vidas = 3;
    }

    private void fimDoJogo() {
        jogoEmAndamento = false;
        jogoPausado = false;
        jogoVencido = false;
        jogador.reset();
        inimigos.clear();
        projeteis.clear();
        vidas = 3;
        requestFocusInWindow();
        temporizador.stop();

        tabelaClassificacao.add(new Pontuacao(nomeJogador, pontuacao));
        tabelaClassificacao.sort(Comparator.comparingInt(Pontuacao::getPontos).reversed());
        tabelaClassificacao = tabelaClassificacao.stream().limit(5).collect(Collectors.toList());

        JOptionPane.showMessageDialog(this, "Fim de Jogo!\nPontuação: " + pontuacao);
        exibirTabelaClassificacao();
    }

    private void exibirTabelaClassificacao() {
        StringBuilder mensagem = new StringBuilder("Tabela de Classificação:\n");
        for (int i = 0; i < tabelaClassificacao.size(); i++) {
            Pontuacao pontuacao = tabelaClassificacao.get(i);
            mensagem.append((i + 1)).append(". ").append(pontuacao.getNome()).append(": ").append(pontuacao.getPontos()).append("\n");
        }
        JOptionPane.showMessageDialog(this, mensagem.toString());
        iniciarJogo();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (jogoEmAndamento) {
            jogador.mover();

            for (int i = projeteis.size() - 1; i >= 0; i--) {
                Projetil projetil = projeteis.get(i);
                projetil.mover();
                if (projetil.getY() < 0) {
                    projeteis.remove(i);
                } else {
                    for (int j = inimigos.size() - 1; j >= 0; j--) {
                        Inimigo inimigo = inimigos.get(j);
                        if (projetil.intersects(inimigo)) {
                            projeteis.remove(i);
                            inimigos.remove(j);
                            pontuacao += 10;
                            break;
                        }
                    }
                }
            }

            for (int i = inimigos.size() - 1; i >= 0; i--) {
                Inimigo inimigo = inimigos.get(i);
                inimigo.mover();
                if (jogador.intersects(inimigo)) {
                    inimigos.remove(i);
                    vidas--;
                    if (vidas == 0) {
                        fimDoJogo();
                    }
                }
            }

            if (inimigos.size() < 5) {
                Random rand = new Random();
                if (rand.nextDouble() < 0.02) {
                    int inimigoX = rand.nextInt(LARGURA_TELA - LARGURA_INIMIGO);
                    int inimigoY = 0;
                    inimigos.add(new Inimigo(inimigoX, inimigoY));
                }
            }

            if (pontuacao >= PONTUACAO_MAXIMA) {
                jogoVencido = true;
                jogoEmAndamento = false;
            }

            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();
        if (jogoEmAndamento) {
            if (!jogoPausado) {
                if (tecla == KeyEvent.VK_LEFT) {
                    moverEsquerda = true;
                } else if (tecla == KeyEvent.VK_RIGHT) {
                    moverDireita = true;
                } else if (tecla == KeyEvent.VK_UP) {
                    moverCima = true;
                } else if (tecla == KeyEvent.VK_DOWN) {
                    moverBaixo = true;
                } else if (tecla == KeyEvent.VK_SPACE) {
                    jogador.atirar();
                }
            }
            if (tecla == KeyEvent.VK_P) {
                jogoPausado = !jogoPausado;
            }
        } else if (jogoIniciado && !jogoEmAndamento) {
            if (tecla == KeyEvent.VK_ENTER) {
                reiniciarJogo();
            }
        } else if (!jogoIniciado) {
            if (tecla == KeyEvent.VK_ENTER) {
                iniciarJogo();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int tecla = e.getKeyCode();
        if (jogoEmAndamento) {
            if (tecla == KeyEvent.VK_LEFT) {
                moverEsquerda = false;
            } else if (tecla == KeyEvent.VK_RIGHT) {
                moverDireita = false;
            } else if (tecla == KeyEvent.VK_UP) {
                moverCima = false;
            } else if (tecla == KeyEvent.VK_DOWN) {
                moverBaixo = false;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(imagemFundo, 0, 0, getWidth(), getHeight(), this);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        g.drawString("Nome do Jogo: " + NOME_JOGO, 20, 20);
        g.drawString("Pontuação: " + pontuacao, 20, 40);
        g.drawString("Vidas: " + vidas, 20, 60);

        if (!jogoIniciado) {
            exibirTelaInicio(g);
        }

        if (jogoPausado && jogoEmAndamento) {
            exibirTelaPausa(g);
        }

        if (jogoVencido) {
            exibirTelaVitoria(g);
        }

        if (!jogoEmAndamento && !jogoVencido && jogoIniciado) {
            exibirTelaFimJogo(g);
        }

        if (jogoEmAndamento) {
            jogador.desenhar(g, this);
            for (Projetil projetil : projeteis) {
                projetil.desenhar(g);
            }
            for (Inimigo inimigo : inimigos) {
                inimigo.desenhar(g, this);
            }
        }
    }

    private void exibirTelaInicio(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Pressione ENTER para iniciar", LARGURA_TELA / 4, ALTURA_TELA / 2);
    }

    private void exibirTelaPausa(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Jogo pausado. Pressione P para continuar", LARGURA_TELA / 4, ALTURA_TELA / 2);
    }

    private void exibirTelaFimJogo(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Fim do Jogo! Pressione ENTER para reiniciar", LARGURA_TELA / 4, ALTURA_TELA / 2);
    }

    private void exibirTelaVitoria(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Você venceu! Pressione ENTER para jogar novamente", LARGURA_TELA / 4, ALTURA_TELA / 2);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Space Invaders");
            SpaceInvadersGame jogo = new SpaceInvadersGame();
            frame.add(jogo);
            frame.setSize(LARGURA_TELA, ALTURA_TELA);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    class Player {
        private int x;
        private int y;

        public Player() {
            x = LARGURA_TELA / 2 - LARGURA_JOGADOR / 2;
            y = ALTURA_TELA - ALTURA_JOGADOR - 20;
        }

        public void mover() {
            if (moverEsquerda && x > 0) {
                x -= VELOCIDADE_JOGADOR;
            }
            if (moverDireita && x < LARGURA_TELA - LARGURA_JOGADOR) {
                x += VELOCIDADE_JOGADOR;
            }
            if (moverCima && y > 0) {
                y -= VELOCIDADE_JOGADOR;
            }
            if (moverBaixo && y < ALTURA_TELA - ALTURA_JOGADOR) {
                y += VELOCIDADE_JOGADOR;
            }
        }

        public void atirar() {
            int projetilX = x + LARGURA_JOGADOR / 2 - LARGURA_PROJETIL / 2;
            int projetilY = y;
            projeteis.add(new Projetil(projetilX, projetilY));
        }

        public void desenhar(Graphics g, SpaceInvadersGame observador) {
            g.drawImage(imagemJogador, x, y, LARGURA_JOGADOR, ALTURA_JOGADOR, observador);
        }

        public boolean intersects(Inimigo inimigo) {
            Rectangle retanguloJogador = new Rectangle(x, y, LARGURA_JOGADOR, ALTURA_JOGADOR);
            Rectangle retanguloInimigo = new Rectangle(inimigo.getX(), inimigo.getY(), LARGURA_INIMIGO, ALTURA_INIMIGO);
            return retanguloJogador.intersects(retanguloInimigo);
        }

        public void reset() {
            x = LARGURA_TELA / 2 - LARGURA_JOGADOR / 2;
            y = ALTURA_TELA - ALTURA_JOGADOR - 20;
        }
    }

    class Projetil {
        private int x;
        private int y;

        public Projetil(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void mover() {
            y -= VELOCIDADE_PROJETIL;
        }

        public void desenhar(Graphics g) {
            g.setColor(Color.BLUE);
            g.fillRect(x, y, LARGURA_PROJETIL, ALTURA_PROJETIL);
        }

        public int getY() {
            return y;
        }

        public boolean intersects(Inimigo inimigo) {
            Rectangle retanguloProjetil = new Rectangle(x, y, LARGURA_PROJETIL, ALTURA_PROJETIL);
            Rectangle retanguloInimigo = new Rectangle(inimigo.getX
                    (), inimigo.getY(), LARGURA_INIMIGO, ALTURA_INIMIGO);
            return retanguloProjetil.intersects(retanguloInimigo);
        }
    }

    class Inimigo {
        private int x;
        private int y;

        public Inimigo(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void mover() {
            y += VELOCIDADE_QUEDA_INIMIGO;
        }

        public void desenhar(Graphics g, SpaceInvadersGame observador) {
            g.drawImage(imagemInimigo, x, y, LARGURA_INIMIGO, ALTURA_INIMIGO, observador);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    class Pontuacao {
        private String nome;
        private int pontos;

        public Pontuacao(String nome, int pontos) {
            this.nome = nome;
            this.pontos = pontos;
        }

        public String getNome() {
            return nome;
        }

        public int getPontos() {
            return pontos;
        }
    }
}
