package pl.skifo.mconsole;

public interface ResponseEvaluator {
    
    /**
     * Evaluates if given server response is correct or not
     * 
     * @param response
     * @return evaluation result
     */
    public boolean isOK(ServerResponse response);
}
