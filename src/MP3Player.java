import javazoom.jl.player.Player;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.function.Consumer;

public class MP3Player {
    private JPanel mainPanel;
    private JTextField fileNameText;
    private JButton selectFileButton;
    private JButton playButton;
    private JList<PlayerThread> playingList;
    private JButton stopButton;

    public static void main(String[] args) {
        JFrame frame = new JFrame("MP3Player");
        frame.setContentPane(new MP3Player().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public MP3Player() {
        DefaultListModel<PlayerThread> listModel = new DefaultListModel<>();
        playingList.setModel(listModel);

        selectFileButton.addActionListener(e -> {
            JFileChooser mp3FileChooser = new JFileChooser();
            mp3FileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isFile() && f.exists() && f.getAbsolutePath().endsWith(".mp3");
                }

                @Override
                public String getDescription() {
                    return "Файлы MP3 (*.mp3)";
                }
            });
            if (JFileChooser.APPROVE_OPTION == mp3FileChooser.showOpenDialog(mainPanel)) {
                fileNameText.setText(mp3FileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        playButton.addActionListener(e -> {
            PlayerThread playerThread = new PlayerThread(fileNameText.getText(), (PlayerThread thread) -> {
                JOptionPane.showMessageDialog(mainPanel, String.format("%s закончился!", thread.getFilePath()), "Всё", JOptionPane.INFORMATION_MESSAGE);
                listModel.removeElement(thread);
            });
            listModel.addElement(playerThread);
            playingList.setSelectedValue(playerThread, true);
        });
        stopButton.addActionListener(e -> {
            PlayerThread selectedPlayer = playingList.getSelectedValue();
            if (selectedPlayer != null) {
                selectedPlayer.stop();
            }
        });
    }

    class PlayerThread implements Runnable {
        private final Date created;
        Thread playThread;
        private final String filePath;
        private final Consumer<PlayerThread> consumer;
        Player player;

        public PlayerThread(String filePath, Consumer<PlayerThread> consumer) {
            this.filePath = filePath;
            this.consumer = consumer;
            this.created = new Date();
            playThread = new Thread(this, filePath);
            playThread.start();
        }

        @Override
        public String toString() {
            return String.format("В %HH:MM запущен %s", created, filePath);
        }

        @Override
        public void run() {
            try {
                FileInputStream stream = new FileInputStream(filePath);
                player = new Player(stream);
                player.play();
                if (consumer != null)
                {
                    consumer.accept(this);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, ex.toString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

        public void stop() {
            player.close();
        }

        public String getFilePath() {
            return filePath;
        }
    }
}
