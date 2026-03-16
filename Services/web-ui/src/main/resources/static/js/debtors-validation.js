/**
 * debtors-validation.js
 * Validaciones para el modal "Nuevo Deudor"
 * ─────────────────────────────────────────
 * - Número de documento: solo dígitos, formato automático con guiones
 *     Cédula  → 001-1234567-8  (11 dígitos)
 *     RNC     → 1-23-45678-9   (9 dígitos)
 * - Teléfono: solo dígitos, formato automático 809-555-0000 (10 dígitos)
 * - Bloquea letras en tiempo real + muestra mensaje de error
 *
 * NO modifica la lógica de abrirModal, cerrarModal, submitDeudor ni resetModal.
 * Solo agrega listeners encima de los inputs existentes.
 */

document.addEventListener("DOMContentLoaded", function () {
  /* ─── Referencias ─────────────────────────────── */
  const inputDoc = document.getElementById("inputDocNumber");
  const inputPhone = document.getElementById("inputPhone");
  const selectDoc = document.getElementById("inputDocType");

  if (!inputDoc || !inputPhone || !selectDoc) return; // salir si no está el modal

  /* ─── Inyectar spans de error si no existen ────── */
  function ensureError(inputEl, id, msg) {
    if (document.getElementById(id)) return;
    const span = document.createElement("span");
    span.id = id;
    span.className = "field-error";
    span.innerHTML = `
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11">
        <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/>
      </svg> ${msg}`;
    inputEl.closest(".form-field").appendChild(span);
  }

  ensureError(
    inputDoc,
    "errorDocFormat",
    "Solo se permiten números. Se agregaron los guiones automáticamente.",
  );
  ensureError(
    inputPhone,
    "errorPhoneFormat",
    "Solo se permiten números. Formato: 809-555-0000.",
  );

  function mostrarErrorCampo(id, show) {
    const el = document.getElementById(id);
    if (el) el.classList.toggle("visible", show);
  }

  /* ─── Formato Cédula: 001-1234567-8 ────────────── */
  function formatCedula(digits) {
    // máx 11 dígitos
    digits = digits.slice(0, 11);
    if (digits.length <= 3) return digits;
    if (digits.length <= 10) return digits.slice(0, 3) + "-" + digits.slice(3);
    return (
      digits.slice(0, 3) + "-" + digits.slice(3, 10) + "-" + digits.slice(10)
    );
  }

  /* ─── Formato RNC: 1-23-45678-9 ────────────────── */
  function formatRNC(digits) {
    // máx 9 dígitos
    digits = digits.slice(0, 9);
    if (digits.length <= 1) return digits;
    if (digits.length <= 3) return digits.slice(0, 1) + "-" + digits.slice(1);
    if (digits.length <= 8)
      return (
        digits.slice(0, 1) + "-" + digits.slice(1, 3) + "-" + digits.slice(3)
      );
    return (
      digits.slice(0, 1) +
      "-" +
      digits.slice(1, 3) +
      "-" +
      digits.slice(3, 8) +
      "-" +
      digits.slice(8)
    );
  }

  /* ─── Formato Teléfono: 809-555-0000 ───────────── */
  function formatPhone(digits) {
    // máx 10 dígitos
    digits = digits.slice(0, 10);
    if (digits.length <= 3) return digits;
    if (digits.length <= 6) return digits.slice(0, 3) + "-" + digits.slice(3);
    return (
      digits.slice(0, 3) + "-" + digits.slice(3, 6) + "-" + digits.slice(6)
    );
  }

  /* ─── Handler: Número de Documento ─────────────── */
  inputDoc.addEventListener("input", function () {
    const raw = this.value;
    const digits = raw.replace(/\D/g, ""); // quitar no-dígitos
    const hasLetters = /[a-zA-Z]/.test(raw); // detectar letras

    const tipo = selectDoc.value;
    this.value = tipo === "RNC" ? formatRNC(digits) : formatCedula(digits);

    mostrarErrorCampo("errorDocFormat", hasLetters);

    // también ocultar el error de "requerido" si ya hay valor
    if (digits.length > 0) mostrarErrorCampo("errorDocNumber", false);
  });

  /* Bloquear pegar texto con letras en documento */
  inputDoc.addEventListener("paste", function (e) {
    e.preventDefault();
    const pasted = (e.clipboardData || window.clipboardData).getData("text");
    const digits = pasted.replace(/\D/g, "");
    const tipo = selectDoc.value;
    this.value = tipo === "RNC" ? formatRNC(digits) : formatCedula(digits);
    mostrarErrorCampo("errorDocFormat", pasted !== digits);
  });

  /* ─── Handler: Teléfono ─────────────────────────── */
  inputPhone.addEventListener("input", function () {
    const raw = this.value;
    const digits = raw.replace(/\D/g, "");
    const hasLetters = /[a-zA-Z]/.test(raw);

    this.value = formatPhone(digits);
    mostrarErrorCampo("errorPhoneFormat", hasLetters);
  });

  /* Bloquear pegar texto con letras en teléfono */
  inputPhone.addEventListener("paste", function (e) {
    e.preventDefault();
    const pasted = (e.clipboardData || window.clipboardData).getData("text");
    const digits = pasted.replace(/\D/g, "");
    this.value = formatPhone(digits);
    mostrarErrorCampo("errorPhoneFormat", pasted !== digits);
  });

  /* ─── Actualizar formato al cambiar tipo de doc ── */
  selectDoc.addEventListener("change", function () {
    const digits = inputDoc.value.replace(/\D/g, "");
    inputDoc.value =
      this.value === "RNC" ? formatRNC(digits) : formatCedula(digits);
    mostrarErrorCampo("errorDocFormat", false);
  });

  /* ─── Limpiar errores de formato al resetear modal ─
     Se engancha al resetModal original sin reemplazarlo */
  const _resetModalOriginal = window.resetModal;
  window.resetModal = function () {
    if (_resetModalOriginal) _resetModalOriginal();
    mostrarErrorCampo("errorDocFormat", false);
    mostrarErrorCampo("errorPhoneFormat", false);
  };
});
