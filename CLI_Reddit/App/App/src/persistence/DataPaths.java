package persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the canonical CLI data directory: {@code CLI_Reddit/App/data}.
 * Never prefers the accidental sibling {@code Spring_Reddit/App/data}.
 */
public final class DataPaths {
    private DataPaths() {
    }

    public static Path resolveDataFile(String fileName) {
        List<Path> candidates = buildCandidates(fileName);

        // Pass 1: existing file under CLI_Reddit/App/data (canonical)
        for (Path candidate : candidates) {
            Path abs = safeAbsolute(candidate);
            if (abs != null && Files.isRegularFile(abs) && isCanonicalCliDataPath(abs)) {
                return abs;
            }
        }

        // Pass 2: any existing file that is NOT the wrong Spring_Reddit/App/data stub dir
        Path bestNonStub = null;
        long bestSize = -1;
        for (Path candidate : candidates) {
            Path abs = safeAbsolute(candidate);
            if (abs == null || !Files.isRegularFile(abs) || isWrongProjectRootDataPath(abs)) {
                continue;
            }
            try {
                long size = Files.size(abs);
                if (size > bestSize) {
                    bestSize = size;
                    bestNonStub = abs;
                }
            } catch (Exception ignored) {
                if (bestNonStub == null) {
                    bestNonStub = abs;
                }
            }
        }
        if (bestNonStub != null) {
            return bestNonStub;
        }

        // Pass 3: create canonical CLI_Reddit/App/data (never Spring_Reddit/App/data)
        for (Path candidate : candidates) {
            Path abs = safeAbsolute(candidate);
            if (abs == null || !isCanonicalCliDataPath(abs)) {
                continue;
            }
            try {
                ensureParent(abs);
                return abs;
            } catch (Exception ignored) {
                // try next
            }
        }

        // Last resort: App/data relative to CWD when already inside CLI_Reddit
        Path fallback = Path.of("App", "data", fileName).toAbsolutePath().normalize();
        ensureParent(fallback);
        return fallback;
    }

    public static Path dataDirectory() {
        return resolveDataFile("subreddits.json").getParent();
    }

    public static void ensureParent(Path file) {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (Exception ignored) {
            // best effort
        }
    }

    private static List<Path> buildCandidates(String fileName) {
        String userDir = System.getProperty("user.dir");
        List<Path> candidates = new ArrayList<>();
        // Prefer CLI_Reddit paths first
        candidates.add(Path.of("CLI_Reddit", "App", "data", fileName));
        candidates.add(Path.of(userDir, "CLI_Reddit", "App", "data", fileName));
        candidates.add(Path.of(userDir, "..", "CLI_Reddit", "App", "data", fileName));
        // When CWD is CLI_Reddit or CLI_Reddit/App
        candidates.add(Path.of("App", "data", fileName));
        candidates.add(Path.of(userDir, "App", "data", fileName));
        // When CWD is CLI_Reddit/App/App (typical IDE module root)
        candidates.add(Path.of("..", "data", fileName));
        candidates.add(Path.of(userDir, "..", "data", fileName));
        candidates.add(Path.of("data", fileName));
        return candidates;
    }

    private static Path safeAbsolute(Path path) {
        try {
            return path.toAbsolutePath().normalize();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isCanonicalCliDataPath(Path absolutePath) {
        String normalized = absolutePath.toString().replace('\\', '/');
        return normalized.contains("/CLI_Reddit/App/data/")
                || normalized.endsWith("/CLI_Reddit/App/data")
                || normalized.contains("/CLI_Reddit/App/data");
    }

    /**
     * Rejects {@code <repo>/App/data} created at the Spring_Reddit project root
     * (sibling of CLI_Reddit), which held empty/stub JSON and masked real data.
     */
    private static boolean isWrongProjectRootDataPath(Path absolutePath) {
        String normalized = absolutePath.toString().replace('\\', '/');
        // .../Spring_Reddit/App/data/file — no CLI_Reddit segment before App/data
        int appDataIdx = normalized.lastIndexOf("/App/data/");
        if (appDataIdx < 0) {
            return false;
        }
        String before = normalized.substring(0, appDataIdx);
        return !before.endsWith("/CLI_Reddit");
    }
}
