package cm.afriland.enquete.Service;

import cm.afriland.enquete.model.CustomerProfile;
import cm.afriland.enquete.repository.CustomerProfileRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AudienceService {
    private final CustomerProfileRepository repository;
    public AudienceService(CustomerProfileRepository repository){this.repository=repository;}
    public Preview preview(List<Map<String,Object>> filters){ List<CustomerProfile> all=repository.findTop500ByOrderByIdAsc(); List<CustomerProfile> matched=all.stream().filter(c->matches(c,filters==null?List.of():filters)).toList(); return new Preview(matched.size(),matched.stream().limit(10).map(c->new CustomerView(c.getId(),c.getName(),c.getCustomerType(),c.getAgency(),c.getCity())).toList()); }
    private boolean matches(CustomerProfile c,List<Map<String,Object>> filters){ for(Map<String,Object> f:filters){String field=String.valueOf(f.getOrDefault("field","")); String value=String.valueOf(f.getOrDefault("value","")); String actual=switch(field){case "customerType"->c.getCustomerType();case "agency"->c.getAgency();case "city"->c.getCity();case "relationshipStatus"->c.getRelationshipStatus();case "product"->c.getProduct();default->null;}; if(actual==null||!actual.equalsIgnoreCase(value)) return false;} return true; }
    public record Preview(long count,List<CustomerView> examples){}
    public record CustomerView(Long id,String name,String customerType,String agency,String city){}
}
