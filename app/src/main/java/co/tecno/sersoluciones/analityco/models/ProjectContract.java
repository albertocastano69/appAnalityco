package co.tecno.sersoluciones.analityco.models;


@SuppressWarnings("FieldCanBeLocal")
public class ProjectContract {
    private String StartDate;
    private String FinishDate;
    private int[] ProjectStages;
    private String ProjectId;
    public String ContractId;

    public ProjectContract(String startDate, String finishDate, int[] projectStages, String projectId) {
        StartDate = startDate;
        FinishDate = finishDate;
        ProjectStages = projectStages;
        ProjectId = projectId;
    }
}
