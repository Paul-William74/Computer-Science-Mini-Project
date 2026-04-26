import graph.Graph;
import graph.Node;
import imageprocessing.ImageProcessor;
import similarity.KNNClassifier;
import similarity.TrainingLoader;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class MainUI extends JFrame {

    private JLabel selectedFileLabel;
    private JLabel predictionLabel;
    private JLabel statusLabel;
    private JLabel imagePreviewLabel;

    private JTextArea neighborsArea;
    private JSpinner kSpinner;

    private File selectedFile;
    private KNNClassifier<Node, Integer> knn;

    public MainUI() {
        setTitle("MediGraph - Retinal Vessel Graph Classification");
        setSize(1250, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initialiseUI();
    }

    private void initialiseUI() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(new Color(245, 247, 250));

        root.add(createHeaderPanel(), BorderLayout.NORTH);
        root.add(createMainPanel(), BorderLayout.CENTER);
        root.add(createFooterPanel(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(32, 67, 116));
        header.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("MediGraph");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 34));

        JLabel subtitle = new JLabel("Graph-Based Retinal Vessel Analysis using KNN and GDD Features");
        subtitle.setForeground(new Color(226, 234, 245));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);

        return header;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new GridLayout(1, 2, 16, 16));
        main.setOpaque(false);

        main.add(createLeftPanel());
        main.add(createRightPanel());

        return main;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(createCardBorder("Input and Controls"));

        panel.add(createControlsAndPredictionPanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createControlsAndPredictionPanel() {
        JPanel container = new JPanel();
        container.setBackground(Color.WHITE);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(new EmptyBorder(16, 16, 16, 16));

        JButton loadTrainingButton = new JButton("Load Training Data");
        JButton chooseImageButton = new JButton("Choose Test Image");
        JButton classifyButton = new JButton("Run Classification");

        styleButton(loadTrainingButton, new Color(56, 121, 217));
        styleButton(chooseImageButton, new Color(77, 155, 101));
        styleButton(classifyButton, new Color(221, 98, 74));

        kSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 15, 1));
        kSpinner.setFont(new Font("SansSerif", Font.PLAIN, 15));
        kSpinner.setPreferredSize(new Dimension(70, 36));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        row1.setBackground(Color.WHITE);
        row1.add(loadTrainingButton);
        row1.add(new JLabel("K Value:"));
        row1.add(kSpinner);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        row2.setBackground(Color.WHITE);
        row2.add(chooseImageButton);
        row2.add(classifyButton);

        selectedFileLabel = new JLabel("Selected file: None");
        selectedFileLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));

        predictionLabel = new JLabel("Prediction: Not classified yet");
        predictionLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        predictionLabel.setForeground(new Color(32, 67, 116));

        statusLabel = new JLabel("Status: Waiting for training data");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 17));
        statusLabel.setForeground(new Color(90, 90, 90));

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        row3.setBackground(Color.WHITE);
        row3.add(selectedFileLabel);

        JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        row4.setBackground(Color.WHITE);
        row4.add(predictionLabel);


        JPanel row5 = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        row5.setBackground(Color.WHITE);
        row5.add(statusLabel);

        loadTrainingButton.addActionListener(e -> loadTrainingData());
        chooseImageButton.addActionListener(e -> chooseImage());
        classifyButton.addActionListener(e -> classifyImage());

        container.add(row1);
        container.add(row2);
        container.add(Box.createVerticalStrut(12));
        container.add(row3);
        container.add(row4);
        container.add(row5);
        container.add(Box.createVerticalGlue());

        return container;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(createCardBorder("Image Preview and Results"));

        panel.add(createLargeImagePanel(), BorderLayout.CENTER);
        panel.add(createNeighborsPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLargeImagePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Selected Test Image Preview");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(35, 35, 35));

        imagePreviewLabel = new JLabel("No image selected", SwingConstants.CENTER);
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setBackground(new Color(245, 247, 250));
        imagePreviewLabel.setBorder(new LineBorder(new Color(210, 215, 220), 1));
        imagePreviewLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        imagePreviewLabel.setPreferredSize(new Dimension(650, 500));
        imagePreviewLabel.setHorizontalAlignment(JLabel.CENTER);
        imagePreviewLabel.setVerticalAlignment(JLabel.CENTER);

        panel.add(title, BorderLayout.NORTH);
        panel.add(imagePreviewLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createNeighborsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new TitledBorder(
                        new LineBorder(new Color(210, 215, 220), 1),
                        "Nearest Neighbors",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("SansSerif", Font.BOLD, 16)
                ),
                new EmptyBorder(8, 8, 8, 8)
        ));

        neighborsArea = new JTextArea(6, 40);
        neighborsArea.setEditable(false);
        neighborsArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        neighborsArea.setLineWrap(true);
        neighborsArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(neighborsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setBackground(new Color(245, 247, 250));

        JLabel label = new JLabel(
                "Presentation flow: Load training data → Select a test image → Run classification"
        );
        label.setFont(new Font("SansSerif", Font.ITALIC, 14));
        label.setForeground(new Color(95, 95, 95));

        footer.add(label);
        return footer;
    }

    private Border createCardBorder(String title) {
        return new CompoundBorder(
                new LineBorder(new Color(218, 223, 228), 1, true),
                new CompoundBorder(
                        new TitledBorder(
                                new LineBorder(new Color(218, 223, 228), 1, true),
                                title,
                                TitledBorder.LEFT,
                                TitledBorder.TOP,
                                new Font("SansSerif", Font.BOLD, 17),
                                new Color(32, 67, 116)
                        ),
                        new EmptyBorder(8, 8, 8, 8)
                )
        );
    }

    private void styleButton(JButton button, Color color) {
        button.setFocusPainted(false);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setPreferredSize(new Dimension(200, 48));
    }

    private void loadTrainingData() {
        statusLabel.setText("Status: Loading training data...");

        JDialog loadingDialog = new JDialog(this, "Loading", true);
        JLabel loadingLabel = new JLabel("Loading training data, please wait...");
        loadingLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        loadingLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        loadingDialog.add(loadingLabel);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(this);

        SwingWorker<KNNClassifier<Node, Integer>, Void> worker = new SwingWorker<>() {
            @Override
            protected KNNClassifier<Node, Integer> doInBackground() {
                int k = (int) kSpinner.getValue();
                return TrainingLoader.loadTrainingData(k);
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    knn = get();
                    statusLabel.setText("Status: Training data loaded successfully");
                    JOptionPane.showMessageDialog(MainUI.this, "Training data loaded successfully.");
                } catch (Exception ex) {
                    statusLabel.setText("Status: Error loading training data");
                    JOptionPane.showMessageDialog(
                            MainUI.this,
                            "Error loading training data: " + ex.getMessage()
                    );
                    ex.printStackTrace();
                }
            }
        };

        worker.execute();
        loadingDialog.setVisible(true);
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            selectedFileLabel.setText("Selected file: " + selectedFile.getName());
            statusLabel.setText("Status: Test image selected");
            showImagePreview(selectedFile);
        }
    }

    private void showImagePreview(File file) {
        try {
            BufferedImage img = ImageIO.read(file);

            if (img == null) {
                imagePreviewLabel.setText("Preview not available");
                imagePreviewLabel.setIcon(null);
                return;
            }

            int labelWidth = imagePreviewLabel.getWidth();
            int labelHeight = imagePreviewLabel.getHeight();

            if (labelWidth <= 0) labelWidth = 650;
            if (labelHeight <= 0) labelHeight = 500;

            int imgWidth = img.getWidth();
            int imgHeight = img.getHeight();

            double scale = Math.min(
                    (double) labelWidth / imgWidth,
                    (double) labelHeight / imgHeight
            );

            int newWidth = (int) (imgWidth * scale);
            int newHeight = (int) (imgHeight * scale);

            Image scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

            imagePreviewLabel.setText("");
            imagePreviewLabel.setIcon(new ImageIcon(scaled));
            imagePreviewLabel.setHorizontalAlignment(JLabel.CENTER);
            imagePreviewLabel.setVerticalAlignment(JLabel.CENTER);

        } catch (Exception e) {
            imagePreviewLabel.setText("Preview not supported");
            imagePreviewLabel.setIcon(null);
        }
    }

    private void classifyImage() {
        if (knn == null) {
            JOptionPane.showMessageDialog(this, "Load training data first.");
            return;
        }

        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Choose a test image first.");
            return;
        }

        statusLabel.setText("Status: Running classification...");

        try {
            ImageProcessor processor = new ImageProcessor();
            Graph<Node, Integer> graph = processor.buildGraph(selectedFile);

            String prediction = knn.predict(graph);
            List<String> neighbors = knn.getNearestNeighbors(graph);

            if (prediction.equalsIgnoreCase("healthy")) {
                predictionLabel.setForeground(new Color(34, 139, 34));
            } else {
                predictionLabel.setForeground(new Color(200, 50, 50));
            }

            predictionLabel.setText("Prediction: " + prediction.toUpperCase());

            neighborsArea.setText("");
            for (String neighbor : neighbors) {
                neighborsArea.append(neighbor + "\n");
            }

            statusLabel.setText("Status: Classification completed");

        } catch (Exception ex) {
            statusLabel.setText("Status: Classification error");
            JOptionPane.showMessageDialog(this, "Classification error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainUI ui = new MainUI();
            ui.setVisible(true);
        });
    }
}