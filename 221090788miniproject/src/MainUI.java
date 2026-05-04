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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MainUI extends JFrame {

    private final Color PRIMARY = new Color(12, 74, 110);
    private final Color PRIMARY_LIGHT = new Color(224, 242, 254);
    private final Color BACKGROUND = new Color(241, 245, 249);
    private final Color CARD = Color.WHITE;
    private final Color TEXT_DARK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(71, 85, 105);
    private final Color BORDER = new Color(203, 213, 225);
    private final Color HEALTHY = new Color(22, 101, 52);
    private final Color DISEASED = new Color(153, 27, 27);

    private CardLayout cardLayout;
    private JPanel appRoot;

    private JLabel originalPreview;
    private JLabel skeletonPreview;

    private JLabel resultLabel;
    private JLabel selectedFileLabel;
    private JLabel statusLabel;

    private JLabel comparisonValue;
    private JLabel outcomeValue;
    private JLabel recommendationValue;
    private JLabel bifurcationValue;

    private JButton loadButton;
    private JButton chooseButton;
    private JButton screenButton;
    private JButton cancelButton;
    private JButton addHealthyButton;
    private JButton addDiseasedButton;

    private JProgressBar loadingBar;

    private File selectedFile;
    private KNNClassifier<Node, Double> classifier;

    private Timer loadingTimer;
    private int loadingStep = 0;

    private final String[] loadingMessages = {
            "Preparing reference images...",
            "Reading retinal vessel patterns...",
            "Building skeleton image...",
            "Counting bifurcations...",
            "Generating screening result..."
    };

    public MainUI() {
        setTitle("MediGraph - Retinal Vessel Screening");
        setSize(1450, 850);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        appRoot = new JPanel(cardLayout);

        appRoot.add(createWelcomePage(), "WELCOME");
        appRoot.add(createScreeningPage(), "SCREENING");

        setContentPane(appRoot);
        cardLayout.show(appRoot, "WELCOME");
    }

    private JPanel createWelcomePage() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(40, 40, 40, 40));

        JPanel card = new JPanel();
        card.setBackground(CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(55, 70, 55, 70)
        ));

        JLabel logo = new JLabel("●", SwingConstants.CENTER);
        logo.setFont(new Font("SansSerif", Font.BOLD, 54));
        logo.setForeground(PRIMARY);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("MediGraph");
        title.setFont(new Font("SansSerif", Font.BOLD, 52));
        title.setForeground(PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Graph-Based Retinal Vessel Screening");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 22));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = new JButton("Start Screening");
        styleButton(startButton, PRIMARY, 240, 48);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> cardLayout.show(appRoot, "SCREENING"));

        JButton exitButton = new JButton("Exit");
        styleOutlineButton(exitButton, 240, 44);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener(e -> System.exit(0));

        card.add(logo);
        card.add(Box.createVerticalStrut(8));
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));
        card.add(Box.createVerticalStrut(38));
        card.add(startButton);
        card.add(Box.createVerticalStrut(12));
        card.add(exitButton);

        root.add(card);
        return root;
    }

    private JPanel createScreeningPage() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.setBackground(BACKGROUND);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createMainPanel(), BorderLayout.CENTER);

        return root;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("MediGraph");
        title.setFont(new Font("SansSerif", Font.BOLD, 38));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Retinal Vessel Screening Dashboard");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 17));
        subtitle.setForeground(PRIMARY_LIGHT);

        text.add(title);
        text.add(Box.createVerticalStrut(5));
        text.add(subtitle);

        JButton homeButton = new JButton("Home");
        styleOutlineHeaderButton(homeButton);
        homeButton.addActionListener(e -> cardLayout.show(appRoot, "WELCOME"));

        header.add(text, BorderLayout.WEST);
        header.add(homeButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createMainPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        JScrollPane leftScroll = new JScrollPane(createLeftPanel());
        leftScroll.setBorder(null);
        leftScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftScroll,
                createRightPanel()
        );

        splitPane.setResizeWeight(0.58);
        splitPane.setDividerLocation(800);
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);

        container.add(splitPane, BorderLayout.CENTER);
        return container;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(CARD);
        panel.setBorder(cardBorder("Screening Dashboard"));

        panel.add(createControlsPanel(), BorderLayout.NORTH);
        panel.add(createResultsPanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        loadButton = new JButton("Load References");
        chooseButton = new JButton("Choose Image");
        screenButton = new JButton("Run Screening");
        cancelButton = new JButton("Cancel Image");
        addHealthyButton = new JButton("Add to Healthy");
        addDiseasedButton = new JButton("Add to Diseased");

        styleButton(loadButton, PRIMARY, 180, 44);
        styleButton(chooseButton, PRIMARY, 180, 44);
        styleButton(screenButton, PRIMARY, 180, 44);
        styleOutlineButton(cancelButton, 180, 44);
        styleOutlineButton(addHealthyButton, 180, 44);
        styleOutlineButton(addDiseasedButton, 180, 44);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        row1.setBackground(CARD);
        row1.add(loadButton);
        row1.add(chooseButton);
        row1.add(screenButton);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        row2.setBackground(CARD);
        row2.add(addHealthyButton);
        row2.add(addDiseasedButton);
        row2.add(cancelButton);

        selectedFileLabel = new JLabel("Selected image: None");
        selectedFileLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        selectedFileLabel.setForeground(TEXT_DARK);

        statusLabel = new JLabel("Status: Ready");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 15));
        statusLabel.setForeground(TEXT_MUTED);

        loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        loadingBar.setStringPainted(true);

        loadButton.addActionListener(e -> loadReferenceImages());
        chooseButton.addActionListener(e -> chooseImage());
        screenButton.addActionListener(e -> runScreening());
        cancelButton.addActionListener(e -> cancelImage());
        addHealthyButton.addActionListener(e -> addToDataset("healthy"));
        addDiseasedButton.addActionListener(e -> addToDataset("diseased"));

        panel.add(row1);
        panel.add(row2);
        panel.add(Box.createVerticalStrut(8));
        panel.add(selectedFileLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(loadingBar);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(CARD);
        panel.setBorder(new EmptyBorder(12, 18, 18, 18));

        resultLabel = new JLabel("Result: Not screened yet");
        resultLabel.setOpaque(true);
        resultLabel.setBackground(new Color(226, 232, 240));
        resultLabel.setForeground(TEXT_DARK);
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 23));
        resultLabel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel metrics = new JPanel(new GridLayout(2, 2, 12, 12));
        metrics.setBackground(CARD);

        comparisonValue = new JLabel("-");
        outcomeValue = new JLabel("-");
        recommendationValue = new JLabel("-");
        bifurcationValue = new JLabel("-");

        metrics.add(metricCard("Image Comparison", comparisonValue));
        metrics.add(metricCard("Screening Outcome", outcomeValue));
        metrics.add(metricCard("Estimated Bifurcation Count", bifurcationValue));
        metrics.add(metricCard("Recommendation", recommendationValue));

        panel.add(resultLabel, BorderLayout.NORTH);
        panel.add(metrics, BorderLayout.CENTER);

        return panel;
    }

    private JPanel metricCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(PRIMARY_LIGHT);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(186, 230, 253), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(PRIMARY);

        JButton help = helpButton(getHelpText(title));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(titleLabel, BorderLayout.WEST);
        top.add(help, BorderLayout.EAST);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        valueLabel.setForeground(TEXT_DARK);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JButton helpButton(String message) {
        JButton button = new JButton("?");
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(26, 26));
        button.setBackground(Color.WHITE);
        button.setForeground(PRIMARY);
        button.setBorder(new LineBorder(PRIMARY, 1, true));

        button.addActionListener(e ->
                JOptionPane.showMessageDialog(this, message, "Help", JOptionPane.INFORMATION_MESSAGE)
        );

        return button;
    }

    private String getHelpText(String title) {
        switch (title) {
            case "Image Comparison":
                return "Shows whether the selected image is closer to healthy or diseased reference images.";
            case "Screening Outcome":
                return "Shows the risk level based on the k-NN classification result.";
            case "Estimated Bifurcation Count":
                return "Estimates vessel branching points from the skeletonized retinal vessel structure.";
            case "Recommendation":
                return "Provides a suggested next step based on the screening outcome.";
            default:
                return "";
        }
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 12, 12));
        panel.setBackground(CARD);
        panel.setBorder(cardBorder("Image Preview"));

        originalPreview = previewLabel("Original image will appear here");
        skeletonPreview = previewLabel("Skeletonized vessel structure will appear after screening");

        panel.add(wrapPreview("Original Retinal Image", originalPreview));
        panel.add(wrapPreview("Skeletonized Vessel Structure", skeletonPreview));

        return panel;
    }

    private JPanel wrapPreview(String title, JLabel label) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD);
        panel.setBorder(new TitledBorder(
                new LineBorder(BORDER, 1, true),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14),
                PRIMARY
        ));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JLabel previewLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(248, 250, 252));
        label.setBorder(new LineBorder(BORDER, 1));
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        label.setForeground(TEXT_MUTED);
        return label;
    }

    private Border cardBorder(String title) {
        return new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new CompoundBorder(
                        new TitledBorder(
                                new LineBorder(BORDER, 1, true),
                                title,
                                TitledBorder.LEFT,
                                TitledBorder.TOP,
                                new Font("SansSerif", Font.BOLD, 17),
                                PRIMARY
                        ),
                        new EmptyBorder(8, 8, 8, 8)
                )
        );
    }

    private void styleButton(JButton button, Color color, int width, int height) {
        button.setFocusPainted(false);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(width, height));
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
    }

    private void styleOutlineButton(JButton button, int width, int height) {
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(PRIMARY);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(width, height));
        button.setBorder(new LineBorder(PRIMARY, 1, true));
    }

    private void styleOutlineHeaderButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(new LineBorder(PRIMARY_LIGHT, 1, true));
        button.setPreferredSize(new Dimension(120, 42));
    }

    private void loadReferenceImages() {
        setLoading(true, "Status: Loading reference images...");
        setButtonsEnabled(false);

        SwingWorker<KNNClassifier<Node, Double>, Void> worker = new SwingWorker<>() {
            @Override
            protected KNNClassifier<Node, Double> doInBackground() {
                return TrainingLoader.loadTrainingData(3);
            }

            @Override
            protected void done() {
                try {
                    classifier = get();
                    setLoading(false, "Status: Reference images loaded successfully.");
                    setButtonsEnabled(true);
                } catch (Exception e) {
                    setLoading(false, "Status: Reference images failed to load.");
                    setButtonsEnabled(true);
                    JOptionPane.showMessageDialog(MainUI.this, "Error loading reference images: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();

            selectedFileLabel.setText("Selected image: " + selectedFile.getName());
            statusLabel.setText("Status: Image selected. Run screening when ready.");

            showImage(selectedFile, originalPreview);

            skeletonPreview.setText("Skeletonized vessel structure will appear after screening");
            skeletonPreview.setIcon(null);

            resetResults();
        }
    }

    private void addToDataset(String label) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please choose an image first.");
            return;
        }

        try {
            File folder = new File("src/Datasets/knn/" + label);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File destination = new File(folder, selectedFile.getName());

            Files.copy(
                    selectedFile.toPath(),
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            JOptionPane.showMessageDialog(this,
                    "Image added to " + label + " dataset.\nReload references before screening again.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not add image: " + e.getMessage());
        }
    }

    private void cancelImage() {
        selectedFile = null;

        selectedFileLabel.setText("Selected image: None");
        statusLabel.setText("Status: Image selection cancelled.");

        originalPreview.setIcon(null);
        originalPreview.setText("Original image will appear here");

        skeletonPreview.setIcon(null);
        skeletonPreview.setText("Skeletonized vessel structure will appear after screening");

        resetResults();
    }

    private void resetResults() {
        resultLabel.setText("Result: Ready for screening");
        resultLabel.setBackground(new Color(226, 232, 240));
        resultLabel.setForeground(TEXT_DARK);

        comparisonValue.setText("-");
        outcomeValue.setText("-");
        recommendationValue.setText("-");
        bifurcationValue.setText("-");
    }

    private void runScreening() {
        if (classifier == null) {
            JOptionPane.showMessageDialog(this, "Please load reference images first.");
            return;
        }

        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please choose an eye image first.");
            return;
        }

        setLoading(true, "Status: Screening retinal image...");
        setButtonsEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String prediction;
            private int bifurcations;

            @Override
            protected Void doInBackground() {
                try {
                    ImageProcessor processor = new ImageProcessor();
                    Graph<Node, Double> graph = processor.buildNodeGraph(selectedFile);

                    prediction = classifier.predict(graph);
                    bifurcations = processor.getLastBifurcationCount();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    updateResults(prediction, bifurcations);
                    showImage(new File("src/Datasets/output/skeleton.png"), skeletonPreview);

                    setLoading(false, "Status: Screening completed.");
                    setButtonsEnabled(true);

                } catch (Exception e) {
                    setLoading(false, "Status: Screening failed.");
                    setButtonsEnabled(true);
                    JOptionPane.showMessageDialog(MainUI.this, "Error during screening: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void updateResults(String prediction, int bifurcations) {
        boolean healthy = prediction.equalsIgnoreCase("healthy");

        bifurcationValue.setText(String.valueOf(bifurcations));

        if (healthy) {
            resultLabel.setText("Result: HEALTHY PATTERN DETECTED");
            resultLabel.setBackground(new Color(220, 252, 231));
            resultLabel.setForeground(HEALTHY);

            comparisonValue.setText("Similar to healthy references");
            outcomeValue.setText("Low concern");
            recommendationValue.setText("No urgent concern indicated");

        } else {
            resultLabel.setText("Result: POSSIBLE DISEASED PATTERN DETECTED");
            resultLabel.setBackground(new Color(254, 226, 226));
            resultLabel.setForeground(DISEASED);

            comparisonValue.setText("Similar to diseased references");
            outcomeValue.setText("Higher concern");
            recommendationValue.setText("Further review advised");
        }
    }

    private void showImage(File file, JLabel label) {
        try {
            BufferedImage img = ImageIO.read(file);

            if (img == null) {
                label.setText("Image preview not available");
                label.setIcon(null);
                return;
            }

            int labelWidth = label.getWidth() <= 0 ? 500 : label.getWidth();
            int labelHeight = label.getHeight() <= 0 ? 320 : label.getHeight();

            double scale = Math.min(
                    (double) labelWidth / img.getWidth(),
                    (double) labelHeight / img.getHeight()
            );

            int newWidth = (int) (img.getWidth() * scale);
            int newHeight = (int) (img.getHeight() * scale);

            Image scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

            label.setText("");
            label.setIcon(new ImageIcon(scaled));

        } catch (Exception e) {
            label.setText("Image preview not available");
            label.setIcon(null);
        }
    }

    private void setLoading(boolean loading, String message) {
        loadingBar.setVisible(loading);
        statusLabel.setText(message);

        if (loading) {
            loadingStep = 0;
            loadingBar.setString(loadingMessages[0]);

            loadingTimer = new Timer(900, e -> {
                loadingStep = (loadingStep + 1) % loadingMessages.length;
                loadingBar.setString(loadingMessages[loadingStep]);
                statusLabel.setText("Status: " + loadingMessages[loadingStep]);
            });

            loadingTimer.start();

        } else {
            if (loadingTimer != null) {
                loadingTimer.stop();
            }

            loadingBar.setString("");
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        loadButton.setEnabled(enabled);
        chooseButton.setEnabled(enabled);
        screenButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
        addHealthyButton.setEnabled(enabled);
        addDiseasedButton.setEnabled(enabled);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainUI().setVisible(true));
    }
}