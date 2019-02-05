package eu.javaexperience.web.dispatch.url.spec.compile;

public interface WebCompiler
{
	public boolean isProduction();
	public void setProduction(boolean val);
	public void compile(boolean incremental);
	public void emitMergedOutput(String destinationFile);
	public long getSourcesLastModification();
}
