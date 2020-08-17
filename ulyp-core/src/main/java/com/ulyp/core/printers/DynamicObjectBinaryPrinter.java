package com.ulyp.core.printers;

import com.ulyp.core.ClassDescription;
import com.ulyp.core.DecodingContext;
import com.ulyp.core.AgentRuntime;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;
import com.ulyp.core.printers.bytes.BinaryOutputAppender;

public class DynamicObjectBinaryPrinter extends ObjectBinaryPrinter {

    protected DynamicObjectBinaryPrinter(int id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return type.isInterface() || type.isExactlyJavaLangObject() || type.isTypeVar();
    }

    @Override
    public Printable read(ClassDescription classDescription, BinaryInput binaryInput, DecodingContext decodingContext) {
        long printerId = binaryInput.readLong();
        return ObjectBinaryPrinterType.printerForId(printerId).read(classDescription, binaryInput, decodingContext);
    }

    @Override
    public void write(Object obj, BinaryOutput out, AgentRuntime agentRuntime) throws Exception {
        Class<?> type = obj.getClass();
        ObjectBinaryPrinter printer = (type != Object.class)
                ? Printers.getInstance().determinePrinterForType(agentRuntime.toType(obj.getClass())) :
                ObjectBinaryPrinterType.IDENTITY_PRINTER.getPrinter();

        try (BinaryOutputAppender appender = out.appender()) {
            appender.append(printer.getId());
            printer.write(obj, appender, agentRuntime);
        }
    }
}
