// ==========================================================================
// You_can: JECRC University Placement Portal
// Client-side Application Logic & Modern Micro-Interactions
// ==========================================================================

document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    initBackToTop();
    initKeyboardShortcuts();
    highlightActiveNav();
});

/**
 * Initializes and manages Dark / Light mode toggle
 */
function initTheme() {
    const savedTheme = localStorage.getItem('youcan-theme') || 'light';
    setTheme(savedTheme);

    const themeToggleBtn = document.getElementById('themeToggleBtn');
    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-bs-theme') || 'light';
            const nextTheme = currentTheme === 'dark' ? 'light' : 'dark';
            setTheme(nextTheme);
        });
    }
}

function setTheme(theme) {
    document.documentElement.setAttribute('data-bs-theme', theme);
    localStorage.setItem('youcan-theme', theme);

    const themeIcon = document.getElementById('themeToggleIcon');
    if (themeIcon) {
        if (theme === 'dark') {
            themeIcon.className = 'bi bi-sun-fill text-warning';
        } else {
            themeIcon.className = 'bi bi-moon-stars-fill text-dark';
        }
    }
}

/**
 * Floating Back-To-Top Button
 */
function initBackToTop() {
    const btn = document.getElementById('btnBackToTop');
    if (!btn) return;

    window.addEventListener('scroll', () => {
        if (window.scrollY > 240) {
            btn.classList.add('show');
        } else {
            btn.classList.remove('show');
        }
    }, { passive: true });

    btn.addEventListener('click', () => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
}

/**
 * Global Keyboard Shortcuts (Ctrl+K or '/' to focus search)
 */
function initKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        // Only trigger if user isn't already inside an input or textarea
        const activeTag = document.activeElement ? document.activeElement.tagName.toLowerCase() : '';
        if (activeTag === 'input' || activeTag === 'textarea' || activeTag === 'select') {
            return;
        }

        if ((e.ctrlKey && e.key === 'k') || (e.metaKey && e.key === 'k') || e.key === '/') {
            const searchInput = document.getElementById('searchInput') || document.querySelector('input[type="search"]') || document.querySelector('input[name="search"]');
            if (searchInput) {
                e.preventDefault();
                searchInput.focus();
                searchInput.select();
            }
        }
    });
}

/**
 * Dynamically highlights the active link in the top navbar and mobile bottom bar
 */
function highlightActiveNav() {
    const currentPath = window.location.pathname.toLowerCase();

    // Bottom nav links
    document.querySelectorAll('.bottom-nav-item').forEach(link => {
        const href = link.getAttribute('href');
        if (href && (href.toLowerCase() === currentPath || (href.length > 1 && currentPath.startsWith(href.toLowerCase())))) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });

    // Top navbar links
    document.querySelectorAll('.navbar-nav .nav-link').forEach(link => {
        const href = link.getAttribute('href');
        if (href && (href.toLowerCase() === currentPath || (href.length > 1 && currentPath.startsWith(href.toLowerCase())))) {
            link.classList.add('active');
        }
    });
}

/**
 * Quick copy helper with tooltip feedback
 */
function copyToClipboard(text, triggerEl) {
    if (!navigator.clipboard) return;
    navigator.clipboard.writeText(text).then(() => {
        if (triggerEl) {
            const originalTitle = triggerEl.getAttribute('title') || '';
            triggerEl.setAttribute('title', 'Copied!');
            triggerEl.classList.add('text-success');
            setTimeout(() => {
                triggerEl.setAttribute('title', originalTitle);
                triggerEl.classList.remove('text-success');
            }, 2000);
        }
    });
}

/**
 * Quick 1-click test login autofill helper for the login page
 */
function fillDemoLogin(email, password) {
    const emailInput = document.getElementById('emailInput');
    const passwordInput = document.getElementById('passwordInput');
    if (emailInput && passwordInput) {
        emailInput.value = email;
        passwordInput.value = password;
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.submit();
        }
    }
}
