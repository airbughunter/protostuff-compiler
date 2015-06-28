package io.protostuff.compiler.parser;

import io.protostuff.compiler.model.AbstractDescriptor;
import io.protostuff.compiler.model.DynamicMessage;
import io.protostuff.compiler.model.SourceCodeLocation;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class OptionParseListener extends AbstractProtoParsetListener {

    public static final int HEX = 16;
    public static final int OCT = 8;
    public static final int DECIMAL = 10;

    private final Deque<DynamicMessage> textFormatStack;
    private DynamicMessage currentTextFormat;
    private DynamicMessage lastTextFormat;

    public OptionParseListener(ProtoContext context) {
        super(context);
        this.textFormatStack = new ArrayDeque<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void exitOption(ProtoParser.OptionContext ctx) {
        String optionName;
        AbstractDescriptor declaration = context.peek(AbstractDescriptor.class);
        ProtoParser.OptionValueContext optionValueContext = ctx.optionValue();
        optionName = ctx.optionName().getText();
        DynamicMessage.Value optionValue = getOptionValue(optionValueContext);
        SourceCodeLocation sourceCodeLocation = getSourceCodeLocation(optionValueContext);
        declaration.getOptions().set(sourceCodeLocation, optionName, optionValue);
    }

    private DynamicMessage.Value getOptionValue(ProtoParser.OptionValueContext optionValueContext) {
        DynamicMessage.Value optionValue;
        SourceCodeLocation sourceCodeLocation = getSourceCodeLocation(optionValueContext);
        if (optionValueContext.textFormat() != null) {
            optionValue = DynamicMessage.Value.createMessage(sourceCodeLocation, lastTextFormat);
        } else if (optionValueContext.BOOLEAN_VALUE() != null) {
            String text = optionValueContext.BOOLEAN_VALUE().getText();
            boolean value = Boolean.parseBoolean(text);
            optionValue = DynamicMessage.Value.createBoolean(sourceCodeLocation, value);
        } else if (optionValueContext.INTEGER_VALUE() != null) {
            String text = optionValueContext.INTEGER_VALUE().getText();
            optionValue = parseInteger(sourceCodeLocation, text);
        } else if (optionValueContext.STRING_VALUE() != null) {
            String text = optionValueContext.STRING_VALUE().getText();
            // TODO: unescape
            optionValue = DynamicMessage.Value.createString(sourceCodeLocation, Util.removeFirstAndLastChar(text));
        } else if (optionValueContext.NAME() != null) {
            String text = optionValueContext.NAME().getText();
            optionValue = DynamicMessage.Value.createEnum(sourceCodeLocation, text);
        } else if (optionValueContext.FLOAT_VALUE() != null) {
            String text = optionValueContext.FLOAT_VALUE().getText();
            double value;
            if ("inf".equals(text)) {
                value = Double.POSITIVE_INFINITY;
            } else if ("-inf".equals(text)) {
                value = Double.NEGATIVE_INFINITY;
            } else {
                value = Double.parseDouble(text);
            }
            optionValue = DynamicMessage.Value.createFloat(sourceCodeLocation, value);
        } else {
            throw new IllegalStateException();
        }
        return optionValue;
    }

    private DynamicMessage.Value parseInteger(SourceCodeLocation sourceCodeLocation, String text) {
        long value;
        try {
            value = Long.decode(text);
        } catch (NumberFormatException e) {
            // For values like "FFFFFFFFFFFFFFFF" - valid unsigned int64 in hex
            // but Long.decode gives NumberFormatException (but we should
            // accept such numbers according to protobuf spec - )
            if (text.startsWith("0x")) {
                BigInteger num = new BigInteger(text.substring(2), HEX);
                value = num.longValue();
            } else if (text.startsWith("0")) {
                BigInteger num = new BigInteger(text.substring(1), OCT);
                value = num.longValue();
            } else {
                BigInteger num = new BigInteger(text, DECIMAL);
                value = num.longValue();
            }
        }
        return DynamicMessage.Value.createInteger(sourceCodeLocation, value);
    }

    @Override
    public void enterTextFormat(ProtoParser.TextFormatContext ctx) {
        if (currentTextFormat != null) {
            textFormatStack.push(currentTextFormat);
        }
        currentTextFormat = new DynamicMessage();
    }

    @Override
    public void exitTextFormat(ProtoParser.TextFormatContext ctx) {
        lastTextFormat = currentTextFormat;
        if (textFormatStack.peek() != null) {
            currentTextFormat = textFormatStack.pop();
        } else {
            currentTextFormat = null;
        }
    }

    @Override
    public void exitTextFormatEntry(ProtoParser.TextFormatEntryContext ctx) {
        String name = ctx.textFormatOptionName().getText();
        DynamicMessage.Value value = getTextFormatOptionValue(ctx);
        currentTextFormat.set(getSourceCodeLocation(ctx), name, value);
    }

    private DynamicMessage.Value getTextFormatOptionValue(ProtoParser.TextFormatEntryContext ctx) {
        DynamicMessage.Value optionValue;
        SourceCodeLocation sourceCodeLocation = getSourceCodeLocation(ctx);
        if (ctx.textFormat() != null) {
            optionValue = DynamicMessage.Value.createMessage(sourceCodeLocation, lastTextFormat);
        } else if (ctx.textFormatOptionValue().BOOLEAN_VALUE() != null) {
            String text = ctx.textFormatOptionValue().BOOLEAN_VALUE().getText();
            boolean value = Boolean.parseBoolean(text);
            optionValue = DynamicMessage.Value.createBoolean(sourceCodeLocation, value);
        } else if (ctx.textFormatOptionValue().INTEGER_VALUE() != null) {
            String text = ctx.textFormatOptionValue().INTEGER_VALUE().getText();
            optionValue = parseInteger(sourceCodeLocation, text);
        } else if (ctx.textFormatOptionValue().STRING_VALUE() != null) {
            String text = ctx.textFormatOptionValue().STRING_VALUE().getText();
            // TODO: unescape
            optionValue = DynamicMessage.Value.createString(sourceCodeLocation, Util.removeFirstAndLastChar(text));
        } else if (ctx.textFormatOptionValue().NAME() != null) {
            String text = ctx.textFormatOptionValue().NAME().getText();
            optionValue = DynamicMessage.Value.createEnum(sourceCodeLocation, text);
        } else if (ctx.textFormatOptionValue().FLOAT_VALUE() != null) {
            String text = ctx.textFormatOptionValue().FLOAT_VALUE().getText();
            double value = Double.parseDouble(text);
            optionValue = DynamicMessage.Value.createFloat(sourceCodeLocation, value);
        } else {
            throw new IllegalStateException();
        }
        return optionValue;
    }

}
