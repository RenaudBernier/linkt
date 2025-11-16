import React, { useRef, useEffect, useState } from 'react';

interface CodeInputProps {
    value: string;
    onChange: (value: string) => void;
    onComplete?: (value: string) => void;
    disabled?: boolean;
    length?: number;
    error?: boolean;
    placeholder?: string;
}

export default function CodeInput({
    value,
    onChange,
    onComplete,
    disabled = false,
    length = 6,
    error = false,
    placeholder = 'Verification code'
}: CodeInputProps) {
    const inputRefs = useRef<(HTMLInputElement | null)[]>([]);
    const [displayValue, setDisplayValue] = useState('');

    // Update display value with formatting (XXX XXX)
    useEffect(() => {
        if (value.length <= 3) {
            setDisplayValue(value);
        } else {
            setDisplayValue(value.slice(0, 3) + ' ' + value.slice(3, 6));
        }
    }, [value]);

    const handleInputChange = (index: number, char: string) => {
        // Only allow digits
        if (!/^\d*$/.test(char)) return;

        // Get current code and update it
        const codeArray = value.split('');
        codeArray[index] = char;
        const newCode = codeArray.slice(0, length).join('');

        onChange(newCode);

        // Auto-move to next field if digit entered
        if (char && index < length - 1) {
            inputRefs.current[index + 1]?.focus();
        }

        // Call onComplete if all fields are filled
        if (newCode.length === length && onComplete) {
            onComplete(newCode);
        }
    };

    const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Backspace') {
            e.preventDefault();

            const codeArray = value.split('');
            codeArray[index] = '';
            const newCode = codeArray.slice(0, length).join('');

            onChange(newCode);

            // Move to previous field on backspace
            if (index > 0) {
                inputRefs.current[index - 1]?.focus();
            }
        } else if (e.key === 'ArrowLeft' && index > 0) {
            inputRefs.current[index - 1]?.focus();
        } else if (e.key === 'ArrowRight' && index < length - 1) {
            inputRefs.current[index + 1]?.focus();
        }
    };

    const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
        e.preventDefault();
        const pastedData = e.clipboardData.getData('text').replace(/\D/g, '');

        if (pastedData.length > 0) {
            const newCode = pastedData.slice(0, length);
            onChange(newCode);

            // Auto-focus last input or first empty
            if (newCode.length === length) {
                inputRefs.current[length - 1]?.focus();
                if (onComplete) {
                    onComplete(newCode);
                }
            } else {
                inputRefs.current[newCode.length]?.focus();
            }
        }
    };

    return (
        <div style={{ marginBottom: 20 }}>
            <label style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>
                {placeholder}
            </label>
            <div
                style={{
                    display: 'flex',
                    gap: '8px',
                    justifyContent: 'center',
                    marginBottom: 8
                }}
            >
                {Array.from({ length }).map((_, index) => (
                    <input
                        key={index}
                        ref={el => (inputRefs.current[index] = el)}
                        type="text"
                        inputMode="numeric"
                        maxLength={1}
                        value={value[index] || ''}
                        onChange={e => handleInputChange(index, e.target.value)}
                        onKeyDown={e => handleKeyDown(index, e)}
                        onPaste={handlePaste}
                        disabled={disabled}
                        data-testid={`code-input-${index}`}
                        style={{
                            width: '45px',
                            height: '45px',
                            fontSize: '20px',
                            textAlign: 'center',
                            border: error ? '2px solid #d32f2f' : '1px solid #ccc',
                            borderRadius: '4px',
                            fontWeight: 'bold',
                            transition: 'border-color 0.2s, box-shadow 0.2s',
                            boxShadow: error
                                ? '0 0 0 3px rgba(211, 47, 47, 0.1)'
                                : value[index]
                                ? '0 0 0 3px rgba(40, 138, 243, 0.1)'
                                : 'none',
                            opacity: disabled ? 0.5 : 1,
                            cursor: disabled ? 'not-allowed' : 'text'
                        }}
                        autoComplete="off"
                    />
                ))}
            </div>
            {error && (
                <div style={{ color: '#d32f2f', fontSize: '12px', marginTop: 4 }}>
                    Invalid code. Please try again.
                </div>
            )}
        </div>
    );
}
