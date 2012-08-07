package models.constraints;

import com.avaje.ebean.ExpressionList;
import org.apache.commons.beanutils.PropertyUtils;
import play.db.ebean.Model;
import play.libs.F.Tuple;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


public class Constraints extends play.data.validation.Constraints {
  @Target({TYPE})
  @Retention(RUNTIME)
  @Constraint(validatedBy = UniqueValidator.class)
  @play.data.Form.Display(name = "constraint.unique")
  
  public static @interface Unique 
  {
    String message() default UniqueValidator.message;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String id();

    String[] fields();
  }

  public static class UniqueValidator extends Validator<Object> implements ConstraintValidator<Unique, Object> 
  {
	  
    final static public String message = "error.unique";
    private String id;
    private String[] fields;

    public void initialize(Unique constraintAnnotation) 
    {
      id = constraintAnnotation.id();
      fields = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(Object o) 
    {
      try 
      {
        Model.Finder<?, ? extends Model> find = new Model.Finder(id.getClass(), o.getClass());

        ExpressionList<?> el = find.where();
        Object idValue = PropertyUtils.getProperty(o, id);
        if (idValue != null) 
        {
          el.ne(id, idValue);
        }
        for (String f : fields) 
        {
          el.eq(f, PropertyUtils.getProperty(o, f));
        }
        return el.findRowCount() == 0;
        
      } 
      catch (Exception e) 
      {
        return false;
      }
    }

    public Tuple<String, Object[]> getErrorMessageKey() 
    {
      return new Tuple<String, Object[]>(message, new Object[]{});
    }
  }
}