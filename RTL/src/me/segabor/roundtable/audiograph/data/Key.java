package me.segabor.roundtable.audiograph.data;

public abstract class Key<T extends Comparable<T>, V extends Comparable<V>>{
	private final T type;
	private final V id;


	public Key(T type, V id) {
		this.type = type;
		this.id = id;
	}

	public final V getId() {
		return id;
	}

	@Deprecated
	public final T getType() {
		return type;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Key other = (Key) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return type.toString() + ":" + id.toString();
	}
}
