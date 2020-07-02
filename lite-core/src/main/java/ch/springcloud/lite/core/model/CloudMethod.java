package ch.springcloud.lite.core.model;

import java.util.List;

import ch.springcloud.lite.core.type.VariantType;
import lombok.Data;

@Data
public class CloudMethod {

	int id;
	String name;
	List<CloudMethodParam> params;
	VariantType returnType;

}
