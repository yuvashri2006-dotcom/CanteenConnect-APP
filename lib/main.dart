import 'package:flutter/material.dart';

const Color forestGreen = Color(0xFF176B45);
const Color pageBackground = Color(0xFFF3F6F3);
const Color darkText = Color(0xFF24372C);
const Color mutedText = Color(0xFF647067);
const Color lightGreen = Color(0xFFE1EEE5);

void main() => runApp(const CanteenConnectApp());

class CanteenConnectApp extends StatelessWidget {
  const CanteenConnectApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'CanteenConnect',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        scaffoldBackgroundColor: pageBackground,
        colorScheme: ColorScheme.fromSeed(seedColor: forestGreen),
        appBarTheme: const AppBarTheme(
          backgroundColor: forestGreen,
          foregroundColor: Colors.white,
          centerTitle: false,
        ),
        inputDecorationTheme: InputDecorationTheme(
          filled: true,
          fillColor: Colors.white,
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(14),
            borderSide: const BorderSide(color: Color(0xFFD7E1D9)),
          ),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(14),
            borderSide: const BorderSide(color: Color(0xFFD7E1D9)),
          ),
        ),
      ),
      home: const LoginScreen(),
    );
  }
}

class BrandMark extends StatelessWidget {
  final double size;
  const BrandMark({super.key, this.size = 58});

  @override
  Widget build(BuildContext context) {
    return Image.asset(
      'assets/images/login_canteenconnect_logo.png',
      width: size,
      height: size,
      fit: BoxFit.contain,
      errorBuilder: (_, __, ___) => Container(
        width: size,
        height: size,
        decoration: BoxDecoration(
          color: forestGreen,
          borderRadius: BorderRadius.circular(size * .25),
        ),
        child: Icon(Icons.link, color: Colors.white, size: size * .6),
      ),
    );
  }
}

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _email = TextEditingController();
  final _password = TextEditingController();
  bool _hidePassword = true;

  @override
  void dispose() {
    _email.dispose();
    _password.dispose();
    super.dispose();
  }

  void _continue() {
    if (!_formKey.currentState!.validate()) return;
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(
        builder: (_) => MainShell(displayName: _email.text.trim().split('@').first),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 440),
              child: Card(
                color: Colors.white,
                elevation: 1,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
                child: Padding(
                  padding: const EdgeInsets.all(26),
                  child: Form(
                    key: _formKey,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        const Center(child: BrandMark(size: 82)),
                        const SizedBox(height: 16),
                        const Text('CanteenConnect',
                          textAlign: TextAlign.center,
                          style: TextStyle(fontSize: 28, fontWeight: FontWeight.w800, color: forestGreen)),
                        const SizedBox(height: 8),
                        const Text('Your canteen, connected.',
                          textAlign: TextAlign.center,
                          style: TextStyle(color: mutedText, fontSize: 15)),
                        const SizedBox(height: 30),
                        const Text('Welcome back', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: darkText)),
                        const SizedBox(height: 6),
                        const Text('Sign in to continue', style: TextStyle(color: mutedText)),
                        const SizedBox(height: 22),
                        TextFormField(
                          controller: _email,
                          keyboardType: TextInputType.emailAddress,
                          decoration: const InputDecoration(labelText: 'Email address', prefixIcon: Icon(Icons.mail_outline)),
                          validator: (v) => (v == null || !v.contains('@')) ? 'Enter a valid email address' : null,
                        ),
                        const SizedBox(height: 14),
                        TextFormField(
                          controller: _password,
                          obscureText: _hidePassword,
                          decoration: InputDecoration(
                            labelText: 'Password',
                            prefixIcon: const Icon(Icons.lock_outline),
                            suffixIcon: IconButton(
                              onPressed: () => setState(() => _hidePassword = !_hidePassword),
                              icon: Icon(_hidePassword ? Icons.visibility_outlined : Icons.visibility_off_outlined),
                            ),
                          ),
                          validator: (v) => (v == null || v.length < 4) ? 'Enter at least 4 characters' : null,
                        ),
                        const SizedBox(height: 22),
                        FilledButton(
                          onPressed: _continue,
                          style: FilledButton.styleFrom(
                            backgroundColor: forestGreen,
                            foregroundColor: Colors.white,
                            minimumSize: const Size.fromHeight(52),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                          ),
                          child: const Text('Sign in', style: TextStyle(fontWeight: FontWeight.bold)),
                        ),
                        const SizedBox(height: 14),
                        const Text('Demo UI: any valid email and a password of 4+ characters will continue.',
                          textAlign: TextAlign.center, style: TextStyle(fontSize: 12, color: mutedText)),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class MainShell extends StatefulWidget {
  final String displayName;
  const MainShell({super.key, required this.displayName});

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell> {
  int _index = 0;
  final List<String> _shopping = [];

  @override
  Widget build(BuildContext context) {
    final pages = [
      HomeTab(name: widget.displayName, onNavigate: (i) => setState(() => _index = i)),
      const CanteensTab(),
      ShoppingTab(items: _shopping, onChanged: () => setState(() {})),
      ProfileTab(name: widget.displayName, onSignOut: () {
        Navigator.of(context).pushAndRemoveUntil(
          MaterialPageRoute(builder: (_) => const LoginScreen()), (_) => false);
      }),
    ];
    return Scaffold(
      appBar: AppBar(
        title: Row(children: [
          const BrandMark(size: 34),
          const SizedBox(width: 10),
          Text(['CanteenConnect', 'Canteens', 'Shopping list', 'Profile'][_index],
            style: const TextStyle(fontWeight: FontWeight.bold)),
        ]),
      ),
      body: IndexedStack(index: _index, children: pages),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home), label: 'Home'),
          NavigationDestination(icon: Icon(Icons.storefront_outlined), selectedIcon: Icon(Icons.storefront), label: 'Canteens'),
          NavigationDestination(icon: Icon(Icons.shopping_basket_outlined), selectedIcon: Icon(Icons.shopping_basket), label: 'Shopping'),
          NavigationDestination(icon: Icon(Icons.person_outline), selectedIcon: Icon(Icons.person), label: 'Profile'),
        ],
      ),
    );
  }
}

class HomeTab extends StatelessWidget {
  final String name;
  final ValueChanged<int> onNavigate;
  const HomeTab({super.key, required this.name, required this.onNavigate});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(18),
      children: [
        Container(
          padding: const EdgeInsets.all(22),
          decoration: BoxDecoration(
            gradient: const LinearGradient(colors: [forestGreen, Color(0xFF2D8B60)]),
            borderRadius: BorderRadius.circular(24),
          ),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            const Text('HELLO,', style: TextStyle(color: Colors.white70, letterSpacing: 1.5, fontWeight: FontWeight.bold)),
            const SizedBox(height: 6),
            Text('${name.isEmpty ? 'there' : name} 👋', style: const TextStyle(color: Colors.white, fontSize: 26, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            const Text('Everything for your canteen, in one place.', style: TextStyle(color: Colors.white)),
          ]),
        ),
        const SizedBox(height: 22),
        const Text('Quick access', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: darkText)),
        const SizedBox(height: 12),
        Row(children: [
          Expanded(child: QuickCard(icon: Icons.storefront, title: 'Find canteens', subtitle: 'Browse places', onTap: () => onNavigate(1))),
          const SizedBox(width: 12),
          Expanded(child: QuickCard(icon: Icons.shopping_basket, title: 'Shopping list', subtitle: 'Manage items', onTap: () => onNavigate(2))),
        ]),
        const SizedBox(height: 20),
        const Text('What you can do', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: darkText)),
        const SizedBox(height: 10),
        const InfoTile(icon: Icons.restaurant_menu, title: 'Explore canteens', subtitle: 'Find canteen information and details.'),
        const InfoTile(icon: Icons.check_circle_outline, title: 'Plan your shopping', subtitle: 'Keep a simple list of items to buy.'),
        const InfoTile(icon: Icons.account_circle_outlined, title: 'Manage your profile', subtitle: 'View your demo profile and sign out.'),
      ],
    );
  }
}

class QuickCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;
  const QuickCard({super.key, required this.icon, required this.title, required this.subtitle, required this.onTap});

  @override
  Widget build(BuildContext context) => Card(
    color: Colors.white,
    elevation: 0,
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18), side: const BorderSide(color: Color(0xFFE0E8E1))),
    child: InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(18),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          CircleAvatar(backgroundColor: lightGreen, foregroundColor: forestGreen, child: Icon(icon)),
          const SizedBox(height: 12),
          Text(title, style: const TextStyle(fontWeight: FontWeight.bold, color: darkText)),
          const SizedBox(height: 4),
          Text(subtitle, style: const TextStyle(fontSize: 12, color: mutedText)),
        ]),
      ),
    ),
  );
}

class InfoTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  const InfoTile({super.key, required this.icon, required this.title, required this.subtitle});

  @override
  Widget build(BuildContext context) => Card(
    color: Colors.white,
    elevation: 0,
    margin: const EdgeInsets.only(bottom: 10),
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
    child: ListTile(
      leading: CircleAvatar(backgroundColor: lightGreen, foregroundColor: forestGreen, child: Icon(icon)),
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.w600, color: darkText)),
      subtitle: Text(subtitle, style: const TextStyle(color: mutedText)),
    ),
  );
}

class CanteensTab extends StatelessWidget {
  const CanteensTab({super.key});

  @override
  Widget build(BuildContext context) {
    final canteens = [
      ('Main Canteen', 'Meals, snacks and beverages', Icons.restaurant),
      ('Fresh Bites', 'Quick bites and refreshments', Icons.local_cafe),
      ('Campus Store', 'Everyday essentials', Icons.store),
    ];
    return ListView(
      padding: const EdgeInsets.all(18),
      children: [
        const Text('Explore canteens', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: darkText)),
        const SizedBox(height: 6),
        const Text('Sample entries — replace with your real canteen data.', style: TextStyle(color: mutedText)),
        const SizedBox(height: 16),
        for (final c in canteens)
          InfoTile(icon: c.$3, title: c.$1, subtitle: c.$2),
      ],
    );
  }
}

class ShoppingTab extends StatefulWidget {
  final List<String> items;
  final VoidCallback onChanged;
  const ShoppingTab({super.key, required this.items, required this.onChanged});

  @override
  State<ShoppingTab> createState() => _ShoppingTabState();
}

class _ShoppingTabState extends State<ShoppingTab> {
  final _itemController = TextEditingController();

  @override
  void dispose() {
    _itemController.dispose();
    super.dispose();
  }

  void _addItem() {
    final value = _itemController.text.trim();
    if (value.isEmpty) return;
    setState(() => widget.items.add(value));
    widget.onChanged();
    _itemController.clear();
  }

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.all(18),
    child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      const Text('My shopping list', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: darkText)),
      const SizedBox(height: 6),
      const Text('Add items you need to remember.', style: TextStyle(color: mutedText)),
      const SizedBox(height: 16),
      Row(children: [
        Expanded(child: TextField(
          controller: _itemController,
          decoration: const InputDecoration(hintText: 'e.g. Rice, milk, apples', prefixIcon: Icon(Icons.add_shopping_cart)),
          onSubmitted: (_) => _addItem(),
        )),
        const SizedBox(width: 8),
        FilledButton(onPressed: _addItem, style: FilledButton.styleFrom(backgroundColor: forestGreen, foregroundColor: Colors.white, padding: const EdgeInsets.all(16)), child: const Icon(Icons.add)),
      ]),
      const SizedBox(height: 12),
      Expanded(child: widget.items.isEmpty
        ? const Center(child: Column(mainAxisSize: MainAxisSize.min, children: [
            Icon(Icons.playlist_add, size: 54, color: Color(0xFF9EAEA2)),
            SizedBox(height: 10),
            Text('Your list is empty', style: TextStyle(fontWeight: FontWeight.bold, color: darkText)),
            Text('Add an item to get started.', style: TextStyle(color: mutedText)),
          ]))
        : ListView.builder(
            itemCount: widget.items.length,
            itemBuilder: (context, index) => Card(
              color: Colors.white,
              child: ListTile(
                leading: const Icon(Icons.shopping_bag_outlined, color: forestGreen),
                title: Text(widget.items[index]),
                trailing: IconButton(
                  tooltip: 'Remove item',
                  icon: const Icon(Icons.delete_outline),
                  onPressed: () {
                    setState(() => widget.items.removeAt(index));
                    widget.onChanged();
                  },
                ),
              ),
            ),
          )),
    ]),
  );
}

class ProfileTab extends StatelessWidget {
  final String name;
  final VoidCallback onSignOut;
  const ProfileTab({super.key, required this.name, required this.onSignOut});

  @override
  Widget build(BuildContext context) => ListView(
    padding: const EdgeInsets.all(18),
    children: [
      const SizedBox(height: 10),
      const Center(child: BrandMark(size: 84)),
      const SizedBox(height: 12),
      Center(child: Text(name.isEmpty ? 'CanteenConnect User' : name, style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: darkText))),
      const Center(child: Text('Demo profile', style: TextStyle(color: mutedText))),
      const SizedBox(height: 24),
      const InfoTile(icon: Icons.person_outline, title: 'Account', subtitle: 'Profile screen placeholder'),
      const InfoTile(icon: Icons.notifications_none, title: 'Notifications', subtitle: 'Notification settings can be added here'),
      const InfoTile(icon: Icons.help_outline, title: 'Help & support', subtitle: 'Support screen placeholder'),
      const SizedBox(height: 12),
      OutlinedButton.icon(
        onPressed: onSignOut,
        icon: const Icon(Icons.logout),
        label: const Text('Sign out'),
        style: OutlinedButton.styleFrom(foregroundColor: forestGreen, minimumSize: const Size.fromHeight(48)),
      ),
    ],
  );
}
