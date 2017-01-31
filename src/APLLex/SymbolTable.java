package APLLex;

public class SymbolTable {
	private String VariableName;
	private String VariableType;
	private int VariableValue;
	
	public SymbolTable(String VariableName, String VariableType, int VariableValue)
	{
		this.VariableName = VariableName;
		this.VariableType = VariableType;
		this.VariableValue = VariableValue;
	}
	
	public String GetName()
	{
		return  (VariableName == null ? "" : VariableName);
	}
	
	public String GetType()
	{
		return (VariableType == null ? "" : VariableType);
	}
	
	public int GetValue()
	{
		return VariableValue;
	}
	
	public void SetValue(int VarValue)
	{
		VariableValue = VarValue;
	}
}
