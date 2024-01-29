package at.qe.skeleton.UtilityClasses;

import jakarta.faces.context.FacesContext;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * This is a Utility Class for mocking FacesContextes in Order to test wether Messages to
 * FacesContext are correctly added and received. I borrowed the code from this website:
 * https://illegalargumentexception.blogspot.com/2011/12/jsf-mocking-facescontext-for-unit-tests.html
 */
public abstract class FacesContextMocker extends FacesContext {
  private FacesContextMocker() {}

  private static final Release RELEASE = new Release();

  private static class Release implements Answer<Void> {
    @Override
    public Void answer(InvocationOnMock invocation) throws Throwable {
      setCurrentInstance(null);
      return null;
    }
  }

  public static FacesContext mockFacesContext() {
    FacesContext context = Mockito.mock(FacesContext.class);
    setCurrentInstance(context);
    Mockito.doAnswer(RELEASE).when(context).release();
    return context;
  }
}
