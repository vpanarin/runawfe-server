package ru.runa.spzavod.validators;

import java.util.regex.Pattern;

import ru.runa.wfe.validation.FieldValidator;
import ru.runa.wfe.var.file.IFileVariable;

/**
 * Валидация файла по следующим критериям: 1. допускается файл нескольких
 * допустимых, форматов 2. длина имени файла не должна превышать N символов, 3.
 * запрет в имени файла любых символов кроме букв и цифр
 *
 * @author dofs
 */
public class FileNameValidator extends FieldValidator {
    private static final Pattern PATTERN = Pattern.compile("^[а-яА-ЯёЁa-zA-Z0-9.]+$");

    @Override
    public void validate() {
        IFileVariable fileVariable = (IFileVariable) getFieldValue();
        if (fileVariable == null) {
            // use a required validator for these
            return;
        }
        String fileName = fileVariable.getName();
        if (fileName == null) {
            addError();
            return;
        }
        String extensionsString = getParameter(String.class, "extensions", null);
        if (extensionsString != null && extensionsString.length() > 0) {
            String[] extensions = extensionsString.split(",");
            boolean accepted = false;
            for (String ext : extensions) {
                if (fileName.toLowerCase().endsWith(ext.toLowerCase())) {
                    accepted = true;
                    break;
                }
            }
            if (!accepted) {
                addError("Данный формат не разрешен к загрузке");
            }
        }
        int maxFileNameLength = getParameter(int.class, "maxFileNameLength", -1);
        if (maxFileNameLength != -1 && fileName.length() > maxFileNameLength) {
            addError("Слишком длинное имя файла");
        }
        if (!PATTERN.matcher(fileName).matches()) {
            addError("Имя файла должно содержать только буквы и цифры");
        }
    }
}
