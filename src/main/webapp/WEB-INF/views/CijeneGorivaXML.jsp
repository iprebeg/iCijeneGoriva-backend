<!-- XML view using a JAXB marshaller -->
<bean id="jaxbView" class="org.springframework.web.servlet.view.xml.MarshallingView">
    <constructor-arg>
        <bean id="jaxb2Marshaller" class="org.springframework.oxm.jaxb.Jaxb2Marshaller">
            <property name="cjenik">
                <list>
                    <value>com.prebeg.proxy.cijenegoriva.model.cjenik</value>
                </list>
            </property>
        </bean>
    </constructor-arg>
</bean>

<!-- Resolve views based on string names -->
<bean class="org.springframework.web.servlet.view.BeanNameViewResolver"/>