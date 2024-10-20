package searchengine;

public class ScoringQuery {

    // 计算词频 (TF)
    public double calculateScoreTF(int wordCount, int documentLength) {
        if (documentLength == 0) {
            return 0;  // 防止除以0的情况
        }
        return (double) wordCount / documentLength;
    }

    // 计算逆文档频率 (IDF)
    public double calculateScoreIDF(int documentCount, int numberOfDocumentsContaingTheTerm) {
        if (numberOfDocumentsContaingTheTerm == 0) {
            return 0;  // 如果没有文档包含该词，返回0
        }
        double idf =  Math.log((1 + documentCount) / (1 +  numberOfDocumentsContaingTheTerm));
        return Math.max(idf, 0.1); 
    }

    // 通过 TF 和 IDF 的乘积计算 TF-IDF
    public double calculateScoreTFIDF(int wordCount, int documentLength, int documentCount,
                                      int numberOfDocumentsContaingTheTerm) {
        double tf = calculateScoreTF(wordCount, documentLength);
        double idf = calculateScoreIDF(documentCount, numberOfDocumentsContaingTheTerm);
        return tf * idf *100;  // 返回 TF-IDF 评分
    }
}