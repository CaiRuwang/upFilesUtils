
package cn.com.vans.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Spring bean helper for get bean in a easy coding way.
 */
@Component
public class BeanFactory implements ApplicationContextAware{
	private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(BeanFactory.applicationContext == null) {
        	BeanFactory.applicationContext = applicationContext;
        }
    }
    /**
	 * Get bean instance that managed by spring with its class.
	 * 
	 * @param typeClass The class
	 * @return The bean that instantiated and managed by spring with the id same as <code>beanId<code>.
	 */
    @SuppressWarnings("unchecked")
	public static <T> T getBean(Class<T> typeClass) {
		return (T)applicationContext.getBean(typeClass);
	}
	/**
	 * Get bean instance that managed by spring with its id.
	 * 
	 * @param beanId The id that defined in spring ApplicationContext-xxx.xml file.
	 * @return The bean that instantiated and managed by spring with the id same as <code>beanId<code>.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String beanId) {
		return (T)applicationContext.getBean(beanId);
	}


	public static ServletRequestAttributes getRequestAttributes() {

		try {
			RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
			ServletRequestAttributes sra = null;
			
			if(null != requestAttributes ){
				sra = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
			}

			return sra;
		} catch (Exception e) {
			return null;
		}
	}

	public static HttpServletRequest getRequest() {

		try {
			ServletRequestAttributes requestAttributes = getRequestAttributes();
			HttpServletRequest request = null;
			
			if(null != requestAttributes){
				request = requestAttributes.getRequest();
			}
			
			return request;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
