package hu.ibello;

import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.GsonBuilder;

import hu.ibello.transform.JsonTransformer;
import hu.ibello.transform.TransformerException;

public class JsonTransformerMock implements JsonTransformer {

	@Override
	public String toJson(Object src) {
		return new GsonBuilder().create().toJson(src);
	}

	@Override
	public String toJson(Object src, Type typeOfSrc) {
		return new GsonBuilder().create().toJson(src, typeOfSrc);
	}

	@Override
	public <T> T fromJson(String json, Type typeOfT) throws TransformerException {
		return new GsonBuilder().create().fromJson(json, typeOfT);
	}

	@Override
	public <T> T fromJson(Reader json, Type typeOfT) throws TransformerException {
		return new GsonBuilder().create().fromJson(json, typeOfT);
	}

	@Override
	public <T> List<T> fromJsonToList(String json, Class<T> typeOfT) throws TransformerException {
		return new GsonBuilder().create().fromJson(json, listType(typeOfT));
	}

	@Override
	public <T> List<T> fromJsonToList(Reader json, Class<T> typeOfT) throws TransformerException {
		return new GsonBuilder().create().fromJson(json, listType(typeOfT));
	}
	
	private <T> Type listType(Class<T> typeOfT) {
		return new ParameterizedType() {

			@Override
			public Type[] getActualTypeArguments() {
				return new Type[] { typeOfT };
			}

			@Override
			public Type getRawType() {
				return List.class;
			}

			@Override
			public Type getOwnerType() {
				return null;
			}
			
		};
	}

}
