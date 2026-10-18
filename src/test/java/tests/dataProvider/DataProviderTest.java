package tests.dataProvider;

import org.testng.annotations.DataProvider;
import utils.ExcelFileManager;
import utils.OpenCSVFileManager;

import java.util.List;

public class DataProviderTest {

    @DataProvider(name = "productFilterData")
    public Object[][] getProductFilterData() {

        String csvPath = "src/main/resources/productFilterData.csv";

        OpenCSVFileManager csvManager = new OpenCSVFileManager(csvPath);

        List<String[]> rows = csvManager.getRows();

        Object[][] data = new Object[rows.size()][3];

        for (int i = 0; i < rows.size(); i++) {

            String[] row = rows.get(i);

            if (row.length < 3) {
                throw new IllegalArgumentException(
                        "Invalid CSV data at row " + (i + 2)
                                + ". Expected 3 columns: minimumPrice, maximumPrice, minimumRating"
                );
            }

            data[i][0] = Double.parseDouble(row[0].trim());
            data[i][1] = Double.parseDouble(row[1].trim());
            data[i][2] = Double.parseDouble(row[2].trim());
        }

        return data;
    }

    @DataProvider(name = "searchData")
    public Object[][] getSearchData() {

        String csvPath = "src/main/resources/searchData.csv";
        OpenCSVFileManager csvManager = new OpenCSVFileManager(csvPath);

        List<String[]> rows = csvManager.getRows();

        Object[][] data = new Object[rows.size()][2];

        for (int i = 0; i < rows.size(); i++) {

            String[] row = rows.get(i);
            if (row.length < 2) {
                throw new IllegalArgumentException(
                        "Invalid CSV data at row " + (i + 2)
                                + ". Expected 2 columns: searchText, expectedMessage"
                );
            }

            data[i][0] = row[0].trim();
            data[i][1] = row[1].trim();
        }

        return data;
    }

    @DataProvider(name = "addMultipleProductsData")
    public static Object[][] getAddProductsData() {

        String excelPath = "src/main/resources/AddMultipleProducts.xlsx";

        ExcelFileManager excel = new ExcelFileManager(excelPath, "AddProducts");

        try {
            List<String> headers = excel.getHeaders();
            List<String[]> rows = excel.getRows();

            int searchIndex = headers.indexOf("searchText");
            int budgetIndex = headers.indexOf("budget");
            int toleranceIndex = headers.indexOf("tolerancePercent");
            int minItemsIndex = headers.indexOf("minItems");

            if (searchIndex < 0 || budgetIndex < 0 || toleranceIndex < 0 || minItemsIndex < 0) {

                throw new IllegalArgumentException(
                        "Excel must contain: searchText, budget, " +
                                "tolerancePercent, minItems"
                );
            }

            Object[][] data = new Object[rows.size()][4];

            for (int i = 0; i < rows.size(); i++) {

                String[] row = rows.get(i);

                data[i][0] = row[searchIndex].trim();
                data[i][1] = Double.parseDouble(row[budgetIndex].trim());
                data[i][2] = Double.parseDouble(row[toleranceIndex].trim());
                data[i][3] = Integer.parseInt(row[minItemsIndex].trim());
            }
            return data;

        } finally {excel.closeExcelFile();}
    }

}
