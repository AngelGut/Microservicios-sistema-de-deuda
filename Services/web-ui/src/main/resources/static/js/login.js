document.addEventListener("DOMContentLoaded", function () {
  const form = document.querySelector("form");
  const btn = document.querySelector(".btn-submit");

  if (form) {
    form.addEventListener("submit", function () {
      btn.disabled = true;
      btn.innerHTML = `
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
                     style="animation: spin 0.8s linear infinite;">
                    <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
                </svg>
                Ingresando...
            `;
    });
  }
});
