// ==========================================================================
// You_can: JECRC University Placement Portal
// Client-side Application Logic & Modern Micro-Interactions
// ==========================================================================

document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    initBackToTop();
    initKeyboardShortcuts();
    highlightActiveNav();
    initMobileInteractions();
});

/**
 * Initializes and manages Dark / Light mode toggle
 */
function initTheme() {
    const savedTheme = localStorage.getItem('youcan-theme') || 'light';
    setTheme(savedTheme);

    const toggleTheme = () => {
        const currentTheme = document.documentElement.getAttribute('data-bs-theme') || 'light';
        const nextTheme = currentTheme === 'dark' ? 'light' : 'dark';
        setTheme(nextTheme);
    };

    const themeToggleBtn = document.getElementById('themeToggleBtn');
    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', toggleTheme);
    }

    document.querySelectorAll('.mobile-theme-btn').forEach(btn => {
        btn.addEventListener('click', toggleTheme);
    });
}

function setTheme(theme) {
    document.documentElement.setAttribute('data-bs-theme', theme);
    localStorage.setItem('youcan-theme', theme);

    const isDark = theme === 'dark';
    const iconClass = isDark ? 'bi bi-sun-fill text-warning' : 'bi bi-moon-stars-fill text-dark';

    const themeIcon = document.getElementById('themeToggleIcon');
    if (themeIcon) {
        themeIcon.className = iconClass;
    }

    document.querySelectorAll('.mobile-theme-icon').forEach(icon => {
        icon.className = iconClass;
    });
}

/**
 * Mobile-specific micro-interactions
 */
function initMobileInteractions() {
    // Automatically close the mobile navbar collapse when tapping a nav link (except dropdown toggles)
    document.querySelectorAll('#navContent .nav-link:not(.dropdown-toggle)').forEach(link => {
        link.addEventListener('click', () => {
            const navCollapse = document.getElementById('navContent');
            if (navCollapse && navCollapse.classList.contains('show')) {
                const bsCollapse = bootstrap.Collapse.getInstance(navCollapse);
                if (bsCollapse) {
                    bsCollapse.hide();
                }
            }
        });
    });
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
