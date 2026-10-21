import 'package:flutter_test/flutter_test.dart';
import 'package:canteen_connect/main.dart';

void main() {
  testWidgets('app starts and shows CanteenConnect', (WidgetTester tester) async {
    await tester.pumpWidget(const CanteenConnectApp());
    expect(find.text('CanteenConnect'), findsOneWidget);
  });
}
