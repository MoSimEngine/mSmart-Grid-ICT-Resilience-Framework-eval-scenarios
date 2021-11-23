package smartgrid.newsimcontrol;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import output.EntityState;
import output.On;
import output.ScenarioResult;
public class AnalysisReportGenerator extends CommonsReportGenerator {
    /**
     * Saves scenario result
     *
     * @param resultFile
     * 		resultFile
     * @param scenarioResult
     * 		scenarioResult
     */
    protected static void saveScenarioResult(final File resultFile, final ScenarioResult scenarioResult) {
        try {
            if (resultFile.exists()) {
                return;
            }
            resultFile.createNewFile();
            final FileWriter fileWriter = new FileWriter(resultFile);
            fileWriter.write(AnalysisReportGenerator.getScenarioResultStats(scenarioResult));
            fileWriter.close();
        } catch (IOException e) {
            System.err.print("Could not write ScenarioResult report to " + resultFile.getAbsolutePath());
        }
    }

    /**
     * Gathers stats and returns them as CSV formatted String
     *
     * @return String of stats in CSV format
     */
    protected static String getScenarioResultStats(ScenarioResult scenarioResult) {
        String headlines = "";
        String content = "";
        Map<String, Integer> stats = new HashMap<>();
        stats.put(CommonsReportGenerator.TOTAL, 0);
        stats.put(CommonsReportGenerator.HACKED_TITLE, 0);
        for (EntityState state : scenarioResult.getStates()) {
            String name = state.getClass().getSimpleName();
            if (stats.get(name) == null) {
                stats.put(name, 0);
            }
            stats.replace(name, stats.get(name).intValue() + 1);
            // Count hacked
            if ((state instanceof On) && ((On) (state)).isIsHacked()) {
                stats.replace(CommonsReportGenerator.HACKED_TITLE, stats.get(CommonsReportGenerator.HACKED_TITLE) + 1);
            }
            // Count Total
            stats.replace(CommonsReportGenerator.TOTAL, stats.get(CommonsReportGenerator.TOTAL).intValue() + 1);
        }
        for (String key : stats.keySet()) {
            if ((content != "") && (headlines != "")) {
                content += ";";
                headlines += ";";
            }
            headlines += key;
            content += stats.get(key).intValue();
        }
        return (headlines + "\n") + content;
    }
}