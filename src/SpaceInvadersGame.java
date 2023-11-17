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
        jogador = new Player(LARGURA_TELA / 2 - LARGURA_JOGADOR / 2, ALTURA_TELA - ALTURA_JOGADOR - 20);
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

    abstract class GameObject {
        private int x;
        private int y;

        public GameObject(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public abstract void mover();

        public Rectangle getBounds(int largura, int altura) {
            return new Rectangle(x, y, largura, altura);
        }
    }

    class Player extends GameObject {
        public Player(int x, int y) {
            super(x, y);
        }

        public void mover() {
            int deltaX = 0;
            int deltaY = 0;

            if (moverEsquerda && getX() > 0) {
                deltaX -= VELOCIDADE_JOGADOR;
            }

            if (moverDireita && getX() < LARGURA_TELA - LARGURA_JOGADOR) {
                deltaX += VELOCIDADE_JOGADOR;
            }

            if (moverCima && getY() > 0) {
                deltaY -= VELOCIDADE_JOGADOR;
            }

            if (moverBaixo && getY() < ALTURA_TELA - ALTURA_JOGADOR) {
                deltaY += VELOCIDADE_JOGADOR;
            }

            setX(getX() + deltaX);
            setY(getY() + deltaY);
        }

        public void atirar() {
            int projetilX = getX() + LARGURA_JOGADOR / 2 - LARGURA_PROJETIL / 2;
            int projetilY = getY();
            projeteis.add(new Projetil(projetilX, projetilY));
        }

        public void desenhar(Graphics g, SpaceInvadersGame observador) {
            g.drawImage(imagemJogador, getX(), getY(), LARGURA_JOGADOR, ALTURA_JOGADOR, observador);
        }

        public boolean intersects(Inimigo inimigo) {
            Rectangle retanguloJogador = getBounds(LARGURA_JOGADOR, ALTURA_JOGADOR);
            Rectangle retanguloInimigo = inimigo.getBounds(LARGURA_INIMIGO, ALTURA_INIMIGO);
            return retanguloJogador.intersects(retanguloInimigo);
        }

        public void reset() {
            setX(LARGURA_TELA / 2 - LARGURA_JOGADOR / 2);
            setY(ALTURA_TELA - ALTURA_JOGADOR - 20);
        }
    }

    class Projetil extends GameObject {
        public Projetil(int x, int y) {
            super(x, y);
        }

        public void mover() {
            setY(getY() - VELOCIDADE_PROJETIL);
        }

        public void desenhar(Graphics g) {
            g.setColor(Color.BLUE);
            g.fillRect(getX(), getY(), LARGURA_PROJETIL, ALTURA_PROJETIL);
        }

        public int getY() {
            return super.getY();
        }

        public boolean intersects(Inimigo inimigo) {
            Rectangle retanguloProjetil = getBounds(LARGURA_PROJETIL, ALTURA_PROJETIL);
            Rectangle retanguloInimigo = inimigo.getBounds(LARGURA_INIMIGO, ALTURA_INIMIGO);
            return retanguloProjetil.intersects(retanguloInimigo);
        }
    }

    class Inimigo extends GameObject {
        public Inimigo(int x, int y) {
            super(x, y);
        }

        public void mover() {
            setY(getY() + VELOCIDADE_QUEDA_INIMIGO);
        }

        public void desenhar(Graphics g, SpaceInvadersGame observador) {
            g.drawImage(imagemInimigo, getX(), getY(), LARGURA_INIMIGO, ALTURA_INIMIGO, observador);
        }

        public int getX() {
            return super.getX();
        }

        public int getY() {
            return super.getY();
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

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Nome do Jogo: ").append(NOME_JOGO).append("\n");
        result.append("Pontuação: ").append(pontuacao).append("\n");
        result.append("Vidas: ").append(vidas).append("\n");

        if (!jogoIniciado) {
            result.append("Pressione ENTER para iniciar\n");
        }

        if (jogoPausado && jogoEmAndamento) {
            result.append("Jogo pausado. Pressione P para continuar\n");
        }

        if (jogoVencido) {
            result.append("Você venceu! Pressione ENTER para jogar novamente\n");
        }

        if (!jogoEmAndamento && !jogoVencido && jogoIniciado) {
            result.append("Fim do Jogo! Pressione ENTER para reiniciar\n");
        }

        return result.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Space Invaders");
            SpaceInvadersGame jogo = new SpaceInvadersGame();
            frame.add(jogo);
            frame.setSize(LARGURA_TELA, ALTURA_TELA);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            // Adiciona um WindowListener para mostrar o toString() quando a janela é fechada
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    JOptionPane.showMessageDialog(frame, jogo.toString());
                }
            });
        });
    }
}