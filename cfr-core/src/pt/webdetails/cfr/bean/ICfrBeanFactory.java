package pt.webdetails.cfr.bean;

public interface ICfrBeanFactory {

  public Object getBean( String id );

  public boolean containsBean( String id );

  public String[] getBeanNamesForType( @SuppressWarnings( "rawtypes" ) Class clazz );

}
