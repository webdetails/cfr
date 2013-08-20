package pt.webdetails.cfr.repository;

public class DefaultFileRepositoryForTests extends DefaultFileRepository {

  @Override
  protected String getBasePath() {
    return "./tests";
  }
}
