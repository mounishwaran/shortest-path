import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class DijkstraMatrixAnimation extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    private static final int ROWS = 20;
    private static final int COLS = 20;
    private int[][] matrix;
    private javax.swing.Timer timer;
    private java.util.List<Point> path = new ArrayList<>();
    private java.util.List<Point> animatedPath = new ArrayList<>();
    private boolean pathFound = false;
    private boolean findingPath = false;
    private boolean obstaclesPlaced = false;
    private int animationIndex = 0;

    private Point startPoint = null;
    private Point endPoint = null;
    private int currentObstacleType = 0; // 0 for black, 1 for brown, 2 for purple, 3 for maroon, 4 for cyan

    public DijkstraMatrixAnimation() {
        matrix = new int[ROWS][COLS];
        initializeMatrix();
        addMouseListener(this);
        addMouseMotionListener(this);
        timer = new javax.swing.Timer(100, this);
        // Set up buttons
        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> startPathfinding());

        JButton blackObstacleButton = new JButton("Black Obstacle");
        blackObstacleButton.addActionListener(e -> currentObstacleType = 0); // Set to black obstacle

        JButton brownObstacleButton = new JButton("Brown Obstacle");
        brownObstacleButton.addActionListener(e -> currentObstacleType = 1); // Set to brown obstacle

        JButton purpleObstacleButton = new JButton("Purple Obstacle");
        purpleObstacleButton.addActionListener(e -> currentObstacleType = 2); // Set to purple obstacle

        JButton maroonObstacleButton = new JButton("Maroon Obstacle");
        maroonObstacleButton.addActionListener(e -> currentObstacleType = 3); // Set to maroon obstacle

        JButton cyanObstacleButton = new JButton("Cyan Obstacle");
        cyanObstacleButton.addActionListener(e -> currentObstacleType = 4); // Set to cyan obstacle

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(blackObstacleButton);
        buttonPanel.add(brownObstacleButton);
        buttonPanel.add(purpleObstacleButton);
        buttonPanel.add(maroonObstacleButton);
        buttonPanel.add(cyanObstacleButton);
        buttonPanel.add(startButton);

        JFrame frame = new JFrame("MPK");
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setSize(600, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void initializeMatrix() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                matrix[i][j] = 1; // Default value, 1 for open cell
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cellWidth = getWidth() / COLS;
        int cellHeight = getHeight() / ROWS;

        // Draw grid with obstacles
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int value = matrix[i][j];
                Color color;
                switch (value) {
                    case 0:
                        color = Color.BLACK; // Black for non-passable obstacles
                        break;
                    case 1:
                        color = new Color(200, 200, 200); // Light gray for open cells
                        break;
                    case 5:
                        color = new Color(139, 69, 19); // Brown for passable obstacles
                        break;
                    case 10:
                        color = Color.MAGENTA; // Purple for passable obstacles
                        break;
                    case 12:
                        color = new Color(128, 0, 0); // Maroon for passable obstacles
                        break;
                    default:
                        color = Color.CYAN; // Cyan for non-passable obstacles
                }
                g.setColor(color);
                g.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
            }
        }

        // Draw start and end points
        if (startPoint != null) {
            g.setColor(Color.BLUE);
            g.fillRect(startPoint.y * cellWidth, startPoint.x * cellHeight, cellWidth, cellHeight);
        }
        if (endPoint != null) {
            g.setColor(Color.RED);
            g.fillRect(endPoint.y * cellWidth, endPoint.x * cellHeight, cellWidth, cellHeight);
        }

        // Draw the animated path in bright green
        g.setColor(Color.GREEN);
        for (int i = 0; i < animationIndex && i < animatedPath.size(); i++) {
            Point p = animatedPath.get(i);
            g.fillRect(p.y * cellWidth, p.x * cellHeight, cellWidth, cellHeight);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (findingPath && animationIndex < animatedPath.size()) {
            animationIndex++;
            repaint();
        } else if (findingPath) {
            findingPath = false;
        }
    }

    private void startPathfinding() {
        if (startPoint != null && endPoint != null && obstaclesPlaced) {
            path = findShortestPath(startPoint, endPoint);
            animatedPath.clear();
            animatedPath.addAll(path);
            findingPath = true;
            animationIndex = 0;
            timer.start();
        }
    }

    private java.util.List<Point> findShortestPath(Point start, Point end) {
        Map<Point, Integer> distance = new HashMap<>();
        Map<Point, Point> previous = new HashMap<>();
        PriorityQueue<Point> queue = new PriorityQueue<>(Comparator.comparingInt(distance::get));

        distance.put(start, 0);
        queue.add(start);
        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (current.equals(end)) break;

            for (Point neighbor : getNeighbors(current)) {
                int cost = matrix[neighbor.x][neighbor.y];
                if (cost == 0) continue; // Skip non-passable obstacles

                int newDist = distance.get(current) + cost;
                if (newDist < distance.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distance.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        java.util.List<Point> path = new ArrayList<>();
        for (Point at = end; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }
    private java.util.List<Point> getNeighbors(Point p) {
        java.util.List<Point> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : directions) {
            int newRow = p.x + d[0];
            int newCol = p.y + d[1];
            if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS) {
                neighbors.add(new Point(newRow, newCol));
            }
        }
        return neighbors;
    }
    @Override
    public void mousePressed(MouseEvent e) {
        int cellWidth = getWidth() / COLS;
        int cellHeight = getHeight() / ROWS;
        int row = e.getY() / cellHeight;
        int col = e.getX() / cellWidth;

        Point selectedPoint = new Point(row, col);
        if (startPoint == null) {
            startPoint = selectedPoint;
        } else if (endPoint == null) {
            endPoint = selectedPoint;
        }
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (startPoint != null && endPoint != null) { // Allow obstacle placement only after start and end points are set
            int cellWidth = getWidth() / COLS;
            int cellHeight = getHeight() / ROWS;
            int row = e.getY() / cellHeight;
            int col = e.getX() / cellWidth;
            if (row >= 0 && row < ROWS && col >= 0 && col < COLS && !new Point(row, col).equals(startPoint) && !new Point(row, col).equals(endPoint)) {
                switch (currentObstacleType) {
                    case 0:
                        matrix[row][col] = 0; // Set cell to 0 for non-passable black obstacle
                        break;
                    case 1:
                        matrix[row][col] = 5; // Set cell to 5 for passable brown obstacle
                        break;
                    case 2:
                        matrix[row][col] = 10; // Set cell to 10 for passable purple obstacle
                        break;
                    case 3:
                        matrix[row][col] = 12; // Set cell to 12 for passable maroon obstacle
                        break;
                    case 4:
                        matrix[row][col] = 0; // Set cell to 0 for non-passable cyan obstacle
                        break;
                }
                obstaclesPlaced = true;
                repaint();
            }
        }
    }
    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DijkstraMatrixAnimation::new);
    }
}
