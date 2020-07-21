package ch.springcloud.lite.core.model;

import ch.springcloud.lite.core.type.VariantType;
import lombok.Data;

@Data
public class CloudMethodParam {

	String name;
	VariantType type;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CloudMethodParam other = (CloudMethodParam) obj;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

}
