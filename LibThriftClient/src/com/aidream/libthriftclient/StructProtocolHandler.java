/**
 * 
 */
package com.aidream.libthriftclient;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TStruct;

import com.aidream.libthriftclient.annotaion.Index;

/**
 * 结构体的 ProtocolHandler
 * 
 * @author HouKangxi
 *
 */
@SuppressWarnings("rawtypes")
class StructProtocolHandler implements ProtocolHandler, Comparator<Field>,
		Cache.IDataCreater<Class, FieldInfo[]> {
	// 假定结构体内的字段都是public声明的(thrift 生成的 Model都是这样)
	public void read(TProtocol protocol, FieldInfo field, IStruct struct)
			throws TException {
		CommonStruct modelStruct = setModelStruct(struct, field, true);
		modelStruct.read(protocol);
	}

	public void write(TProtocol protocol, FieldInfo field, IStruct struct)
			throws TException {
		CommonStruct modelStruct = setModelStruct(struct, field, false);
		modelStruct.write(protocol);
	}

	private CommonStruct setModelStruct(IStruct struct, FieldInfo field,
			boolean createNewBean) throws TException {
		final Class<?> structClass = field.getFieldClass();
		Object o = struct.getFieldValue(field);
		if (createNewBean || o == null) {
			try {
				o = structClass.newInstance();
				struct.setFieldValue(field, o);
			} catch (Throwable ex) {
				throw new TException(ex);
			}
		}
		if (o == null) {
			return null;
		}
		FieldInfo[] fs = Cache.getInstance().getStructCache(structClass, this);
		CommonStruct modelStruct = new CommonStruct(fs, new TStruct(
				structClass.getSimpleName()));
		modelStruct.setStructBean(o);
		return modelStruct;
	}

	@Override
	public FieldInfo[] create(Class structClass, Object... args) {
		Field[] fields_ref = getSortedFields(structClass);
		FieldInfo[] fs = new FieldInfo[fields_ref.length];
		short i = 0;
		for (Field fieldRf : fields_ref) {
			short index = i;
			fs[index] = FieldInfo.parseFieldForType(fieldRf.getGenericType(),
					fieldRf.getName(), ++i);
		}
		return fs;
	}
	
	private Field[] getSortedFields(Class<?> clazz){
		Field[] fs = clazz.getFields();

		List<Field> rslist = new ArrayList<Field>(fs.length);
		for (Field f : fs) {
			// 剔除静态字段
			if (Modifier.isStatic(f.getModifiers())) {
				continue;
			}
			rslist.add(f);
		}
		int size = rslist.size();
		if (fs.length != size) {
			fs = new Field[size];
			rslist.toArray(fs);
		}
		if(TBase.class.isAssignableFrom(clazz)){
			//旧版的未缩减的JavaBean
			try {
				for (Field af : clazz.getDeclaredFields()) {
					if (af.getType() == TField.class) {
						af.setAccessible(true);
						TField tfield = (TField) af.get(null);
						// 按指定顺序重新归位
						fs[tfield.id-1] = clazz.getField(tfield.name);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}else{
			// 按@Index 排序
			Arrays.sort(fs, this);
		}
		return fs;
	}
	
	public int compare(Field o1, Field o2) {
		int ord1 = 0, ord2 = 0;
		Index index1 = o1.getAnnotation(Index.class);
		Index index2 = o2.getAnnotation(Index.class);
		if (index1 != null) {
			ord1 = index1.value();
		}
		if (index2 != null) {
			ord2 = index2.value();
		}
		return ord1 - ord2;
	}
}
