package imageprocessing;

public class Skeletonization {

    private int[][] skeleton=null;
    public Skeletonization(int[][] binary){
       this.skeleton= skeletonize(binary);
    }

    public int[][] getSkeleton(){
        return this.skeleton;
    }
    private int[][] skeletonize(int[][] binary) {
        int width = binary.length;
        int height = binary[0].length;

        boolean changed;
        int[][] img = new int[width][height];

        // Copy binary image
        for (int x = 0; x < width; x++) {
            System.arraycopy(binary[x], 0, img[x], 0, height);
        }

        do {
            changed = false;

            // Step 1
            java.util.List<int[]> toRemove = new java.util.ArrayList<>();

            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {

                    if (img[x][y] != 1) continue;

                    int[] n = getNeighbors(img, x, y);
                    int B = countNonZero(n);
                    int A = countTransitions(n);

                    if (B >= 2 && B <= 6 && A == 1 &&
                            n[0] * n[2] * n[4] == 0 &&
                            n[2] * n[4] * n[6] == 0) {

                        toRemove.add(new int[]{x, y});
                    }
                }
            }

            if (!toRemove.isEmpty()) changed = true;
            for (int[] p : toRemove) img[p[0]][p[1]] = 0;

            // Step 2
            toRemove.clear();

            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {

                    if (img[x][y] != 1) continue;

                    int[] n = getNeighbors(img, x, y);
                    int B = countNonZero(n);
                    int A = countTransitions(n);

                    if (B >= 2 && B <= 6 && A == 1 &&
                            n[0] * n[2] * n[6] == 0 &&
                            n[0] * n[4] * n[6] == 0) {

                        toRemove.add(new int[]{x, y});
                    }
                }
            }

            if (!toRemove.isEmpty()) changed = true;
            for (int[] p : toRemove) img[p[0]][p[1]] = 0;

        } while (changed);

        return img;
    }
    private int[] getNeighbors(int[][] img, int x, int y) {
        return new int[]{
                img[x][y - 1],     // P2
                img[x + 1][y - 1], // P3
                img[x + 1][y],     // P4
                img[x + 1][y + 1], // P5
                img[x][y + 1],     // P6
                img[x - 1][y + 1], // P7
                img[x - 1][y],     // P8
                img[x - 1][y - 1]  // P9
        };
    }

    private int countNonZero(int[] n) {
        int count = 0;
        for (int val : n) if (val == 1) count++;
        return count;
    }

    private int countTransitions(int[] n) {
        int count = 0;
        for (int i = 0; i < n.length; i++) {
            if (n[i] == 0 && n[(i + 1) % n.length] == 1) {
                count++;
            }
        }
        return count;
    }


}
