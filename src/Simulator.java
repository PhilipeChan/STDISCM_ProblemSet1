import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Random;

public class Simulator {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Particle Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JLabel fpsLabel = new JLabel("FPS: 0.00");
        Canvas canvas = new Canvas(fpsLabel);
        frame.add(fpsLabel, BorderLayout.NORTH);
        frame.add(canvas, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        setupInputPanel(inputPanel, canvas);

        JScrollPane scrollPane = new JScrollPane(inputPanel);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(400, 720));
        frame.add(scrollPane, BorderLayout.EAST);

        frame.pack();
        frame.setVisible(true);

        canvas.startSimulation();
    }

    private static int getRandomIntInRange(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    private static double getRandomDoubleInRange(double min, double max) {
        Random random = new Random();
        return min + (max - min) * random.nextDouble();
    }

    private static void setupInputPanel(JPanel panel, Canvas canvas) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel betweenPointsPanel = new JPanel();
        betweenPointsPanel.setLayout(new BoxLayout(betweenPointsPanel, BoxLayout.Y_AXIS));
        betweenPointsPanel.setBorder(BorderFactory.createTitledBorder(null, "Add Particles (Between Points)", TitledBorder.CENTER, TitledBorder.TOP));
        JTextField nField = addLabeledTextField(betweenPointsPanel, "Number of Particles:", "");
        JTextField startXField = addLabeledTextField(betweenPointsPanel, "Start X:", "");
        JTextField startYField = addLabeledTextField(betweenPointsPanel, "Start Y:", "");
        JTextField endXField = addLabeledTextField(betweenPointsPanel, "End X:", "");
        JTextField endYField = addLabeledTextField(betweenPointsPanel, "End Y:", "");
        JTextField angleField = addLabeledTextField(betweenPointsPanel, "Angle:", "");
        JTextField velocityField = addLabeledTextField(betweenPointsPanel, "Velocity:", "");
        JPanel betweenPointsButtonsPanel = new JPanel();
        betweenPointsButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addButton = new JButton("Add Particles");
        addButton.addActionListener(e -> {
            try {
                int n = Integer.parseInt(nField.getText());
                int startX = Integer.parseInt(startXField.getText());
                int startY = Integer.parseInt(startYField.getText());
                int endX = Integer.parseInt(endXField.getText());
                int endY = Integer.parseInt(endYField.getText());
                double angle = Double.parseDouble(angleField.getText());
                double velocity = Double.parseDouble(velocityField.getText());

                if (n < 1) throw new IllegalArgumentException("Number of particles must be at least 1.");

                if (startX < 0 || startX > 1280 || endX < 0 || endX > 1280 || startY < 0 || startY > 720 || endY < 0 || endY > 720) {
                    throw new IllegalArgumentException("X must be between 0 and 1280, Y must be between 0 and 720.");
                }

                canvas.addParticlesBetweenPoints(n, new Point(startX, startY), new Point(endX, endY), angle, velocity);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid input. Please enter valid numbers.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage());
            }
        });
        JButton addRandomParticlesButton = new JButton("Add Random Particles");
        addRandomParticlesButton.addActionListener(e -> {
            int n = getRandomIntInRange(1, 500);
            Point start = new Point(getRandomIntInRange(0, 1280), getRandomIntInRange(0, 720));
            Point end = new Point(getRandomIntInRange(0, 1280), getRandomIntInRange(0, 720));
            double angle = getRandomDoubleInRange(0, 360);
            double velocity = getRandomDoubleInRange(50, 500);

            canvas.addParticlesBetweenPoints(n, start, end, angle, velocity);
        });
        betweenPointsButtonsPanel.add(addButton);
        betweenPointsButtonsPanel.add(addRandomParticlesButton);
        betweenPointsPanel.add(betweenPointsButtonsPanel);
        panel.add(betweenPointsPanel);

        JPanel varyingAnglesPanel = new JPanel();
        varyingAnglesPanel.setLayout(new BoxLayout(varyingAnglesPanel, BoxLayout.Y_AXIS));
        varyingAnglesPanel.setBorder(BorderFactory.createTitledBorder(null, "Add Particles (Varying Angles)", TitledBorder.CENTER, TitledBorder.TOP));
        JTextField nAngleField = addLabeledTextField(varyingAnglesPanel, "Number of Particles:", "");
        JTextField startAngleField = addLabeledTextField(varyingAnglesPanel, "Start Angle:", "");
        JTextField endAngleField = addLabeledTextField(varyingAnglesPanel, "End Angle:", "");
        JTextField velocityAngleField = addLabeledTextField(varyingAnglesPanel, "Velocity:", "");
        JTextField xField = addLabeledTextField(varyingAnglesPanel, "X:", "");
        JTextField yField = addLabeledTextField(varyingAnglesPanel, "Y:", "");
        JPanel varyingAnglesButtonsPanel = new JPanel();
        varyingAnglesButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addAngleButton = new JButton("Add Particles");
        addAngleButton.addActionListener(e -> {
            try {
                int n = Integer.parseInt(nAngleField.getText());
                double startAngle = Double.parseDouble(startAngleField.getText());
                double endAngle = Double.parseDouble(endAngleField.getText());
                double velocity = Double.parseDouble(velocityAngleField.getText());
                int x = Integer.parseInt(xField.getText());
                int y = Integer.parseInt(yField.getText());

                if (n < 1) throw new IllegalArgumentException("Number of particles must be at least 1.");

                if (x < 0 || x > 1280 || y < 0 || y > 720) {
                    throw new IllegalArgumentException("X must be between 0 and 1280, Y must be between 0 and 720.");
                }

                canvas.addParticlesVaryingAngles(n, new Point(x, y), startAngle, endAngle, velocity);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid input. Please enter valid numbers.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage());
            }
        });
        JButton addRandomParticlesVaryingAnglesButton = new JButton("Add Random Particles");
        addRandomParticlesVaryingAnglesButton.addActionListener(e -> {
            int n = getRandomIntInRange(1, 500);
            Point start = new Point(getRandomIntInRange(0, 1280), getRandomIntInRange(0, 720));
            double startAngle = getRandomDoubleInRange(0, 360);
            double endAngle = getRandomDoubleInRange(0, 360);
            double velocity = getRandomDoubleInRange(50, 500);

            canvas.addParticlesVaryingAngles(n, start, startAngle, endAngle, velocity);
        });
        varyingAnglesButtonsPanel.add(addAngleButton);
        varyingAnglesButtonsPanel.add(addRandomParticlesVaryingAnglesButton);
        varyingAnglesPanel.add(varyingAnglesButtonsPanel);
        panel.add(varyingAnglesPanel);

        JPanel varyingVelocitiesPanel = new JPanel();
        varyingVelocitiesPanel.setLayout(new BoxLayout(varyingVelocitiesPanel, BoxLayout.Y_AXIS));
        varyingVelocitiesPanel.setBorder(BorderFactory.createTitledBorder(null, "Add Particles (Varying Velocities)", TitledBorder.CENTER, TitledBorder.TOP));
        JTextField nVelocityField = addLabeledTextField(varyingVelocitiesPanel, "Number of Particles:", "");
        JTextField startVelocityField = addLabeledTextField(varyingVelocitiesPanel, "Start Velocity:", "");
        JTextField endVelocityField = addLabeledTextField(varyingVelocitiesPanel, "End Velocity:", "");
        JTextField angleVelocityField = addLabeledTextField(varyingVelocitiesPanel, "Angle:", "");
        JTextField xVelocityField = addLabeledTextField(varyingVelocitiesPanel, "X:", "");
        JTextField yVelocityField = addLabeledTextField(varyingVelocitiesPanel, "Y:", "");
        JPanel varyingVelocitiesButtonsPanel = new JPanel();
        varyingVelocitiesButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addVelocityButton = new JButton("Add Particles");
        addVelocityButton.addActionListener(e -> {
            try {
                int n = Integer.parseInt(nVelocityField.getText());
                double startVelocity = Double.parseDouble(startVelocityField.getText());
                double endVelocity = Double.parseDouble(endVelocityField.getText());
                double angle = Double.parseDouble(angleVelocityField.getText());
                int x = Integer.parseInt(xVelocityField.getText());
                int y = Integer.parseInt(yVelocityField.getText());

                if (n < 1) throw new IllegalArgumentException("Number of particles must be at least 1.");

                if (x < 0 || x > 1280 || y < 0 || y > 720) {
                    throw new IllegalArgumentException("X must be between 0 and 1280, Y must be between 0 and 720.");
                }

                canvas.addParticlesVaryingVelocities(n, new Point(x, y), angle, startVelocity, endVelocity);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid input. Please enter valid numbers.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage());
            }
        });
        JButton addRandomParticlesVaryingVelocitiesButton = new JButton("Add Random Particles");
        addRandomParticlesVaryingVelocitiesButton.addActionListener(e -> {
            int n = getRandomIntInRange(1, 500);
            Point start = new Point(getRandomIntInRange(0, 1280), getRandomIntInRange(0, 720));
            double angle = getRandomDoubleInRange(0, 360);
            double startVelocity = getRandomDoubleInRange(50, 275);
            double endVelocity = getRandomDoubleInRange(275, 500);

            canvas.addParticlesVaryingVelocities(n, start, angle, startVelocity, endVelocity);
        });
        varyingVelocitiesButtonsPanel.add(addVelocityButton);
        varyingVelocitiesButtonsPanel.add(addRandomParticlesVaryingVelocitiesButton);
        varyingVelocitiesPanel.add(varyingVelocitiesButtonsPanel);
        panel.add(varyingVelocitiesPanel);

        JPanel addWallPanel = new JPanel();
        addWallPanel.setLayout(new BoxLayout(addWallPanel, BoxLayout.Y_AXIS));
        addWallPanel.setBorder(BorderFactory.createTitledBorder(null, "Add Wall", TitledBorder.CENTER, TitledBorder.TOP));
        JTextField x1Field = addLabeledTextField(addWallPanel, "X1:", "");
        JTextField y1Field = addLabeledTextField(addWallPanel, "Y1:", "");
        JTextField x2Field = addLabeledTextField(addWallPanel, "X2:", "");
        JTextField y2Field = addLabeledTextField(addWallPanel, "Y2:", "");
        JPanel addWallButtonsPanel = new JPanel();
        addWallButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addWallButton = new JButton("Add Wall");
        addWallButton.addActionListener(e -> {
            try {
                int x1 = Integer.parseInt(x1Field.getText());
                int y1 = Integer.parseInt(y1Field.getText());
                int x2 = Integer.parseInt(x2Field.getText());
                int y2 = Integer.parseInt(y2Field.getText());

                if (x1 < 0 || x1 > 1280 || x2 < 0 || x2 > 1280 || y1 < 0 || y1 > 720 || y2 < 0 || y2 > 720) {
                    throw new IllegalArgumentException("X must be between 0 and 1280, Y must be between 0 and 720.");
                }

                canvas.addWall(new Wall(x1, y1, x2, y2));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid input for wall coordinates. Please enter valid integers.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage());
            }
        });
        JButton addRandomWallButton = new JButton("Add Random Wall");
        addRandomWallButton.addActionListener(e -> {
            Point start = new Point(getRandomIntInRange(0, 1280), getRandomIntInRange(0, 720));
            Point end = new Point(getRandomIntInRange(0, 1280), getRandomIntInRange(0, 720));

            canvas.addWall(new Wall(start.x, start.y, end.x, end.y));
        });
        addWallButtonsPanel.add(addWallButton);
        addWallButtonsPanel.add(addRandomWallButton);
        addWallPanel.add(addWallButtonsPanel);
        panel.add(addWallPanel);
    }

    private static JTextField addLabeledTextField(JPanel panel, String labelText, String textFieldText) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        JTextField textField = new JTextField(10);
        textField.setText(textFieldText);
        fieldPanel.add(label);
        fieldPanel.add(textField);
        panel.add(fieldPanel);
        return textField;
    }
}